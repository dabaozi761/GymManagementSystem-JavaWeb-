package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.common.convention.utils.CurrentActor;
import com.dabaozi.gymmanagementsystem.mapper.*;
import com.dabaozi.gymmanagementsystem.pojo.dto.*;
import com.dabaozi.gymmanagementsystem.pojo.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ProductMapper productMapper;
    private final ShopOrderMapper orderMapper;
    private final MemberMapper memberMapper;
    private final WalletService walletService;
    private final CurrentActor actor;

    private void validate(Product product) {
        if (product.getName() == null || product.getName().isBlank() || product.getName().length() > 100) throw new ClientException("商品名称必填且最多100字");
        product.setName(product.getName().trim());
        if (product.getCategory() == null || product.getCategory() < 1 || product.getCategory() > 5) throw new ClientException("商品分类只能是1至5");
        WalletService.validateMoney(product.getPrice(), false);
        if (product.getStock() == null || product.getStock() < 0 || product.getStock() > 1000000) throw new ClientException("库存必须在0至1000000之间");
        if (product.getStatus() == null || (product.getStatus() != 0 && product.getStatus() != 1)) throw new ClientException("商品状态只能是0或1");
        if (product.getRemark() != null && product.getRemark().length() > 255) throw new ClientException("备注最多255字");
    }

    public void saveProduct(Product dto) {
        actor.requireAdmin(); validate(dto);
        Product product = new Product(); // 不接受主键、逻辑删除等系统字段。
        copyProduct(dto, product);
        productMapper.insert(product);
    }

    private void copyProduct(Product source, Product target) {
        target.setName(source.getName()); target.setCategory(source.getCategory()); target.setPrice(source.getPrice());
        target.setStock(source.getStock()); target.setStatus(source.getStatus()); target.setRemark(source.getRemark());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductUpdateDTO dto) {
        actor.requireAdmin(); validate(dto);
        if (dto.getId() == null) throw new ClientException("商品ID不能为空");
        Product existing = productMapper.selectForUpdate(dto.getId());
        if (existing == null) throw new ClientException("商品不存在");
        if (!Objects.equals(existing.getStock(), dto.getExpectedStock())) throw new ClientException("库存已变化，请刷新列表后重新编辑");
        if (!Objects.equals(existing.getCategory(), dto.getCategory())) throw new ClientException("商品分类不能修改，请下架后新增商品");
        copyProduct(dto, existing);
        productMapper.updateById(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        actor.requireAdmin();
        Product product = productMapper.selectForUpdate(id);
        if (product == null) throw new ClientException("商品不存在");
        // 只下架，订单快照和在租物品归还仍然有效。
        productMapper.update(null, Wrappers.lambdaUpdate(Product.class).eq(Product::getId, id).set(Product::getStatus, 0));
    }

    public Page<Product> products(CommercePageReqDTO dto) {
        dto.validate();
        Integer status = dto.getStatus();
        if (!actor.isAdmin()) {
            status = 1;
        }
        return productMapper.selectPage(new Page<>(dto.getCurrent(), dto.getSize()), Wrappers.lambdaQuery(Product.class)
                .like(dto.getName() != null && !dto.getName().isBlank(), Product::getName, dto.getName())
                .eq(dto.getCategory() != null, Product::getCategory, dto.getCategory())
                .eq(status != null, Product::getStatus, status).orderByDesc(Product::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public ShopOrder checkout(ShopCheckoutDTO dto) {
        if (actor.isAdmin()) throw new ClientException("请使用会员账号购买或租用商品");
        if (dto.getProductId() == null || dto.getQuantity() == null || dto.getQuantity() < 1 || dto.getQuantity() > 100) throw new ClientException("请选择商品，数量必须在1至100之间");
        if (dto.getRequestNo() == null || !dto.getRequestNo().matches("[A-Za-z0-9-]{16,64}")) throw new ClientException("提交标识无效，请重新打开购买窗口");
        MembershipCard card = walletService.lockUsableCard(dto.getCardId());
        actor.requireOwner(card.getMemberId());
        ShopOrder previous = orderMapper.selectByRequestNo(dto.getRequestNo());
        if (previous != null) {
            actor.requireOwner(previous.getMemberId());
            if (!Objects.equals(previous.getCardId(), dto.getCardId()) || !Objects.equals(previous.getProductId(), dto.getProductId())
                    || !Objects.equals(previous.getQuantity(), dto.getQuantity()) || !Objects.equals(previous.getRentalDays(), dto.getRentalDays() == null ? 0 : dto.getRentalDays())) {
                throw new ClientException("提交标识已使用，请重新下单");
            }
            return previous;
        }
        Product product = productMapper.selectForUpdate(dto.getProductId());
        if (product == null || !Integer.valueOf(1).equals(product.getStatus())) throw new ClientException("商品不存在或已下架");
        boolean rental = product.getCategory() <= 2;
        int days = dto.getRentalDays() == null ? 0 : dto.getRentalDays();
        if (rental && (days < 1 || days > 30)) throw new ClientException("租用天数必须在1至30之间");
        if (!rental && days != 0) throw new ClientException("普通商品不能填写租用天数");
        if (product.getStock() < dto.getQuantity()) throw new ClientException("商品库存不足");
        BigDecimal amount = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())).multiply(BigDecimal.valueOf(rental ? days : 1));
        WalletService.validateMoney(amount, false);
        if (card.getBalance() == null || card.getBalance().compareTo(amount) < 0) throw new ClientException("会员卡余额不足，请联系前台充值");
        ShopOrder order = new ShopOrder();
        order.setOrderNo("SO" + UUID.randomUUID().toString().replace("-", ""));
        order.setRequestNo(dto.getRequestNo()); order.setMemberId(card.getMemberId());
        order.setMemberName(memberMapper.selectById(card.getMemberId()).getName());
        order.setCardId(card.getId()); order.setCardNo(card.getCardNo());
        order.setProductId(product.getId()); order.setProductName(product.getName()); order.setCategory(product.getCategory());
        order.setUnitPrice(product.getPrice()); order.setQuantity(dto.getQuantity()); order.setRentalDays(days);
        order.setAmount(amount); order.setStatus(rental ? 2 : 1);
        if (rental) order.setDueTime(LocalDateTime.now().plusDays(days));
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException ex) {
            throw new ClientException("提交标识已使用，请重新打开购买窗口");
        }
        productMapper.update(null, Wrappers.lambdaUpdate(Product.class).eq(Product::getId, product.getId()).set(Product::getStock, product.getStock() - dto.getQuantity()));
        walletService.changeBalance(card, amount.negate(), 2, order.getId(), (rental ? "租用：" : "购买：") + product.getName());
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnRental(Long id) {
        actor.requireAdmin(); // 前台确认已实际归还后释放库存。
        ShopOrder order = orderMapper.selectForUpdate(id);
        if (order == null || order.getCategory() > 2) throw new ClientException("租用订单不存在");
        if (order.getStatus() == 3) return; // 重复确认不重复增加库存。
        if (order.getStatus() != 2) throw new ClientException("订单不在租用中");
        Product product = productMapper.selectForUpdate(order.getProductId());
        if (product == null) throw new ClientException("租用品不存在，请联系管理员");
        if ((long) product.getStock() + order.getQuantity() > 1000000) throw new ClientException("归还后库存超出上限，请先调整库存");
        productMapper.update(null, Wrappers.lambdaUpdate(Product.class).eq(Product::getId, product.getId()).set(Product::getStock, product.getStock() + order.getQuantity()));
        orderMapper.update(null, Wrappers.lambdaUpdate(ShopOrder.class).eq(ShopOrder::getId, id).set(ShopOrder::getStatus, 3).set(ShopOrder::getReturnTime, LocalDateTime.now()));
    }

    public Page<ShopOrder> orders(CommercePageReqDTO dto) {
        dto.validate(); Long memberId = actor.scopedMemberId(dto.getMemberId());
        return orderMapper.selectPage(new Page<>(dto.getCurrent(), dto.getSize()), Wrappers.lambdaQuery(ShopOrder.class)
                .eq(memberId != null, ShopOrder::getMemberId, memberId)
                .eq(dto.getStatus() != null, ShopOrder::getStatus, dto.getStatus())
                .eq(dto.getCategory() != null, ShopOrder::getCategory, dto.getCategory())
                .orderByDesc(ShopOrder::getId));
    }

    public ShopOrder orderDetail(Long id) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) throw new ClientException("订单不存在");
        actor.requireOwner(order.getMemberId()); return order;
    }
}
