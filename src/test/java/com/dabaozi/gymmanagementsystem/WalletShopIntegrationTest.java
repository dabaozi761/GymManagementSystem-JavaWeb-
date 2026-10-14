package com.dabaozi.gymmanagementsystem;

import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.common.convention.utils.JwtUtils;
import com.dabaozi.gymmanagementsystem.pojo.dto.*;
import com.dabaozi.gymmanagementsystem.pojo.entity.*;
import com.dabaozi.gymmanagementsystem.service.WalletService;
import com.dabaozi.gymmanagementsystem.service.ShopService;
import com.dabaozi.gymmanagementsystem.service.MembershipCardService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 独立内存数据库验证真实事务与行锁，不连接或修改开发数据库。 */
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.profiles.active=test", "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:commerce;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.sql.init.mode=always", "spring.sql.init.schema-locations=classpath:commerce-schema.sql",
        "mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.nologging.NoLoggingImpl"
})
class WalletShopIntegrationTest {
    @Autowired WalletService wallet;
    @Autowired ShopService shop;
    @Autowired MembershipCardService cards;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;

    static void login(int role, long id) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("role", role); request.setAttribute("loginId", id);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
    @BeforeEach void seed() {
        jdbc.update("DELETE FROM card_transaction"); jdbc.update("DELETE FROM shop_order");
        jdbc.update("DELETE FROM shop_product"); jdbc.update("DELETE FROM membership_card"); jdbc.update("DELETE FROM member");
        jdbc.update("INSERT INTO member(id,member_no,user_id,name,status) VALUES(1,'M1',8,'陈可',1),(2,'M2',9,'李沐',1)");
        jdbc.update("INSERT INTO membership_card(id,card_no,member_id,card_type,balance,total_count,remain_count,start_date,end_date,status) VALUES(1,'C1',1,1,100,20,12,?,?,1),(2,'C2',2,4,100,20,12,NULL,NULL,1)", LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        jdbc.update("INSERT INTO shop_product(id,name,category,price,stock,status) VALUES(1,'运动饮料',3,8,10,1),(2,'储物柜',1,5,2,1),(3,'充电宝',2,3,2,1)");
        login(1, 1);
    }
    @AfterEach void logout() { RequestContextHolder.resetRequestAttributes(); }
    BigDecimal balance(long id) { return jdbc.queryForObject("SELECT balance FROM membership_card WHERE id=?", BigDecimal.class, id); }
    int entries() { return jdbc.queryForObject("SELECT COUNT(*) FROM card_transaction", Integer.class); }
    ShopCheckoutDTO checkout(long productId, long cardId, int quantity, int days) {
        ShopCheckoutDTO dto = new ShopCheckoutDTO(); dto.setProductId(productId); dto.setCardId(cardId);
        dto.setQuantity(quantity); dto.setRentalDays(days); dto.setRequestNo(UUID.randomUUID().toString()); return dto;
    }

    @ParameterizedTest @ValueSource(ints = {1, 2, 3, 4})
    void everyCardTypeSupportsRechargeAndMoneyConsumption(int type) {
        jdbc.update("UPDATE membership_card SET card_type=? WHERE id=1", type);
        wallet.recharge(1L, new BigDecimal("50.25"), "现金收款");
        wallet.consume(1L, new BigDecimal("20.10"), null, "消费");
        assertEquals(new BigDecimal("130.15"), balance(1)); assertEquals(2, entries());
        CardTransaction first = wallet.page(new CommercePageReqDTO()).getRecords().get(1);
        assertEquals(new BigDecimal("100.00"), first.getBalanceBefore());
        assertEquals(new BigDecimal("150.25"), first.getBalanceAfter());
    }
    @Test void countConsumptionCreatesCountSnapshotWithoutChangingBalance() {
        wallet.consume(2L, null, 2, "入场核销");
        CardTransaction entry = wallet.page(new CommercePageReqDTO()).getRecords().get(0);
        assertEquals(3, entry.getType()); assertEquals(12, entry.getRemainCountBefore()); assertEquals(10, entry.getRemainCountAfter());
        assertEquals(new BigDecimal("100.00"), balance(2));
    }
    @Test void invalidAndInsufficientDeductionsDoNotWriteAnything() {
        assertThrows(ClientException.class, () -> wallet.consume(1L, new BigDecimal("101"), null, null));
        assertThrows(ClientException.class, () -> wallet.consume(2L, null, 13, null));
        assertThrows(ClientException.class, () -> wallet.consume(1L, BigDecimal.ONE, 1, null));
        assertThrows(ClientException.class, () -> wallet.consume(1L, null, null, null));
        assertThrows(ClientException.class, () -> wallet.consume(1L, null, 1, null));
        assertEquals(0, entries()); assertEquals(new BigDecimal("100.00"), balance(1));
    }
    @ParameterizedTest @ValueSource(strings = {"0", "-1", "0.001", "100000000"})
    void invalidMoneyIsRejected(String amount) {
        assertThrows(ClientException.class, () -> wallet.recharge(1L, new BigDecimal(amount), null));
        assertEquals(0, entries());
    }
    @Test void frozenExpiredFutureAndWithdrawnCardsCannotBeUsed() {
        jdbc.update("UPDATE membership_card SET status=2 WHERE id=1");
        assertThrows(ClientException.class, () -> wallet.recharge(1L, BigDecimal.ONE, null));
        jdbc.update("UPDATE membership_card SET status=1,end_date=? WHERE id=1", LocalDate.now().minusDays(1));
        assertThrows(ClientException.class, () -> wallet.consume(1L, BigDecimal.ONE, null, null));
        jdbc.update("UPDATE membership_card SET start_date=?,end_date=? WHERE id=1", LocalDate.now().plusDays(1), LocalDate.now().plusDays(30));
        assertThrows(ClientException.class, () -> wallet.consume(1L, BigDecimal.ONE, null, null));
        jdbc.update("UPDATE member SET status=0 WHERE id=2");
        assertThrows(ClientException.class, () -> wallet.recharge(2L, BigDecimal.ONE, null));
    }
    @Test void memberCannotRechargeDeductOrWriteProducts() {
        login(3, 8);
        assertThrows(ClientException.class, () -> wallet.recharge(1L, BigDecimal.TEN, null));
        assertThrows(ClientException.class, () -> wallet.consume(1L, BigDecimal.TEN, null, null));
        assertThrows(ClientException.class, () -> shop.saveProduct(new Product()));
        assertThrows(ClientException.class, () -> shop.returnRental(1L));
    }
    @Test void memberCannotOverrideLedgerOrderOrCardOwnership() {
        wallet.recharge(1L, BigDecimal.ONE, null); wallet.recharge(2L, BigDecimal.TEN, null);
        login(3, 9); ShopOrder otherOrder = shop.checkout(checkout(1, 2, 1, 0));
        login(3, 8);
        CommercePageReqDTO filter = new CommercePageReqDTO(); filter.setMemberId(2L);
        assertEquals(1, wallet.page(filter).getTotal());
        assertEquals(1L, wallet.page(filter).getRecords().get(0).getMemberId());
        Long otherEntryId = jdbc.queryForObject("SELECT id FROM card_transaction WHERE member_id=2 AND type=1", Long.class);
        assertThrows(ClientException.class, () -> wallet.detail(otherEntryId));
        assertEquals(0, shop.orders(filter).getTotal());
        assertThrows(ClientException.class, () -> shop.orderDetail(otherOrder.getId()));
        assertThrows(ClientException.class, () -> cards.getCard(2L));
        MembershipCardPageReqDTO cardFilter = new MembershipCardPageReqDTO(); cardFilter.setMemberId(2L);
        assertEquals(1L, cards.pageCard(cardFilter).getRecords().get(0).getMemberId());
        assertThrows(ClientException.class, () -> shop.checkout(checkout(1, 2, 1, 0)));
    }
    @Test void checkoutUsesDatabasePricesCreatesLedgerAndDoesNotChargeTwice() {
        login(3, 8); ShopCheckoutDTO dto = checkout(1, 1, 2, 0);
        ShopOrder order = shop.checkout(dto); ShopOrder retry = shop.checkout(dto);
        assertEquals(order.getId(), retry.getId()); assertEquals(new BigDecimal("84.00"), balance(1));
        assertEquals(8, jdbc.queryForObject("SELECT stock FROM shop_product WHERE id=1", Integer.class));
        assertEquals(1, entries());
        assertEquals(order.getId(), wallet.page(new CommercePageReqDTO()).getRecords().get(0).getOrderId());
        dto.setQuantity(3); assertThrows(ClientException.class, () -> shop.checkout(dto));
    }
    @ParameterizedTest @ValueSource(longs = {2, 3})
    void rentalStockReleasedOnlyOnceByAdministrator(long productId) {
        login(3, 8); ShopOrder order = shop.checkout(checkout(productId, 1, 1, 3));
        assertEquals(2, order.getStatus()); assertNotNull(order.getDueTime());
        assertEquals(1, jdbc.queryForObject("SELECT stock FROM shop_product WHERE id=?", Integer.class, productId));
        assertThrows(ClientException.class, () -> shop.returnRental(order.getId()));
        login(1, 1); shop.deleteProduct(productId); shop.returnRental(order.getId()); shop.returnRental(order.getId());
        assertEquals(2, jdbc.queryForObject("SELECT stock FROM shop_product WHERE id=?", Integer.class, productId));
        assertEquals(3, shop.orderDetail(order.getId()).getStatus()); assertEquals(1, entries());
    }
    @Test void insufficientBalanceStockAndOffShelfCannotCreateOrders() {
        login(3, 8);
        assertThrows(ClientException.class, () -> shop.checkout(checkout(1, 1, 11, 0)));
        assertThrows(ClientException.class, () -> shop.checkout(checkout(2, 1, 1, 30)));
        assertThrows(ClientException.class, () -> shop.checkout(checkout(2, 1, 1, 0)));
        jdbc.update("UPDATE shop_product SET status=0 WHERE id=1");
        assertThrows(ClientException.class, () -> shop.checkout(checkout(1, 1, 1, 0)));
        assertEquals(0, entries()); assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM shop_order", Integer.class));
    }
    @Test void ledgerFailureRollsBackBalanceAndEntireCheckout() {
        jdbc.execute("ALTER TABLE card_transaction ADD CONSTRAINT reject_ledger CHECK (amount < 1)");
        try {
            assertThrows(Exception.class, () -> wallet.recharge(1L, BigDecimal.TEN, null));
            assertEquals(new BigDecimal("100.00"), balance(1));
            login(3, 8); assertThrows(Exception.class, () -> shop.checkout(checkout(1, 1, 2, 0)));
            assertEquals(new BigDecimal("100.00"), balance(1)); assertEquals(0, entries());
            assertEquals(10, jdbc.queryForObject("SELECT stock FROM shop_product WHERE id=1", Integer.class));
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM shop_order", Integer.class));
        } finally { jdbc.execute("ALTER TABLE card_transaction DROP CONSTRAINT reject_ledger"); }
    }
    @Test void simultaneousDeductionsCannotOverdraw() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2); CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> deduction = () -> {
            login(1, 1); start.await();
            try { wallet.consume(1L, new BigDecimal("80"), null, null); return true; }
            catch (ClientException ex) { return false; }
            finally { RequestContextHolder.resetRequestAttributes(); }
        };
        try {
            Future<Boolean> first = pool.submit(deduction), second = pool.submit(deduction); start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertEquals(new BigDecimal("20.00"), balance(1)); assertEquals(1, entries());
        } finally { pool.shutdownNow(); }
    }
    @Test void editingCardPreservesBalanceAndRejectsDirectBalanceChange() {
        MembershipCardUpdateDTO dto = new MembershipCardUpdateDTO(); dto.setId(1L); dto.setMemberId(1L); dto.setCardType(1);
        dto.setStartDate(LocalDate.now().minusDays(1)); dto.setEndDate(LocalDate.now().plusDays(60)); dto.setStatus(1);
        cards.updateCard(dto); assertEquals(new BigDecimal("100.00"), balance(1));
        dto.setBalance(BigDecimal.ZERO); assertThrows(ClientException.class, () -> cards.updateCard(dto));
        assertThrows(ClientException.class, () -> cards.deleteCard(1L));
    }
    @Test void oldTypeFiveIsRejectedAndOpeningBalanceGetsALedger() {
        MembershipCardSaveDTO dto = new MembershipCardSaveDTO(); dto.setMemberId(1L); dto.setStatus(1); dto.setCardType(5);
        assertThrows(ClientException.class, () -> cards.savaCard(dto));
        dto.setCardType(4); dto.setTotalCount(10); dto.setBalance(new BigDecimal("25"));
        cards.savaCard(dto); assertEquals(1, entries());
        assertEquals(new BigDecimal("0.00"), wallet.page(new CommercePageReqDTO()).getRecords().get(0).getBalanceBefore());
    }
    @Test void httpRejectsMemberFinancialWritesAndScopesSignedTokenQueries() throws Exception {
        wallet.recharge(1L, BigDecimal.ONE, null); wallet.recharge(2L, BigDecimal.TEN, null);
        String token = JwtUtils.generateJwt(Map.of("role", 3, "loginId", 8));
        mvc.perform(post("/api/gym-management-system/membershipCard/v1/recharge").header("token", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":1,\"amount\":100}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/gym-management-system/product/v1/save").header("token", token)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/gym-management-system/member/v1/update").header("token", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"id\":2,\"userId\":8}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/gym-management-system/shopOrder/v1/returnRental?id=1").header("token", token))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/gym-management-system/cardTransaction/v1/page").header("token", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"memberId\":2,\"type\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].memberId").value(1));
        mvc.perform(post("/api/gym-management-system/cardTransaction/v1/page")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
    @Test void productValidationAndMemberViewDoNotExposeOffShelfStock() {
        Product product = new Product(); product.setName("蛋白粉"); product.setCategory(4); product.setPrice(new BigDecimal("99.90")); product.setStock(3); product.setStatus(1);
        shop.saveProduct(product);
        product.setPrice(new BigDecimal("0.001")); assertThrows(ClientException.class, () -> shop.saveProduct(product));
        shop.deleteProduct(1L); login(3, 8);
        CommercePageReqDTO filter = new CommercePageReqDTO(); filter.setStatus(0);
        assertTrue(shop.products(filter).getRecords().stream().allMatch(row -> row.getStatus() == 1));
    }
    @ParameterizedTest @NullSource @ValueSource(ints = {0, 1})
    void administratorProductPageAllowsEmptyStatusAndFiltersBothStates(Integer productStatus) throws Exception {
        jdbc.update("UPDATE shop_product SET status=0 WHERE id=1");
        CommercePageReqDTO filter = new CommercePageReqDTO(); filter.setStatus(productStatus);
        int expectedTotal = productStatus == null ? 3 : (productStatus == 1 ? 2 : 1);
        assertEquals(expectedTotal, shop.products(filter).getTotal());
        String token = JwtUtils.generateJwt(Map.of("role", 1, "loginId", 1));
        String body = productStatus == null ? "{}" : "{\"status\":" + productStatus + "}";
        mvc.perform(post("/api/gym-management-system/product/v1/page").header("token", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.total").value(expectedTotal));
    }
    @Test void memberWithoutAssociationCannotReadLedgersOrdersOrBuy() {
        login(2, 999);
        assertThrows(ClientException.class, () -> wallet.page(new CommercePageReqDTO()));
        assertThrows(ClientException.class, () -> shop.orders(new CommercePageReqDTO()));
        assertThrows(ClientException.class, () -> shop.checkout(checkout(1, 1, 1, 0)));
    }
    @Test void staleProductEditCannotRestoreAlreadySoldStock() {
        ProductUpdateDTO edit = new ProductUpdateDTO(); edit.setId(1L); edit.setName("运动饮料新包装"); edit.setCategory(3);
        edit.setPrice(BigDecimal.TEN); edit.setStock(10); edit.setExpectedStock(10); edit.setStatus(1);
        login(3, 8); shop.checkout(checkout(1, 1, 1, 0));
        login(1, 1); assertThrows(ClientException.class, () -> shop.updateProduct(edit));
        assertEquals(9, jdbc.queryForObject("SELECT stock FROM shop_product WHERE id=1", Integer.class));
        edit.setStock(9); edit.setExpectedStock(9); shop.updateProduct(edit);
        assertEquals("运动饮料新包装", jdbc.queryForObject("SELECT name FROM shop_product WHERE id=1", String.class));
    }
}
