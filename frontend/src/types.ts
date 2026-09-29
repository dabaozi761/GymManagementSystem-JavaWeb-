export type Row = Record<string, any>
export interface Session { id: number; username: string; name: string; token: string; role: number }
export interface PageData { records: Row[]; total: number; current: number; size: number; pages: number }
export interface Option { label: string; value: number | string }
export interface Field {
  key: string; label: string; type?: 'text' | 'password' | 'tel' | 'date' | 'number' | 'select' | 'textarea' | 'relation';
  required?: boolean; options?: Option[]; resource?: string; searchKey?: string; labelKey?: string;
  min?: number; max?: number; step?: string; maxLength?: number; placeholder?: string; hint?: string;
  createOnly?: boolean; editOnly?: boolean; visible?: (row: Row) => boolean; default?: any;
}
export interface Column { key: string; label: string; type?: 'date' | 'datetime' | 'money' | 'badge' | 'person'; options?: Option[] }
export interface Resource {
  key: string; title: string; singular: string; subtitle: string; icon: string;
  searchKey?: string; searchPlaceholder?: string; filters: Field[]; columns: Column[]; fields: Field[];
  statuses?: Option[]; statusAction?: boolean; detail?: boolean; createAction?: string; readOnly?: boolean;
  defaultFilters?: Row;
}
