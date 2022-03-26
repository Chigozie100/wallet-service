DROP TABLE IF EXISTS accounts_transactions;
CREATE TABLE accounts_transactions (
    accounts_id bigint NOT NULL,
    transactions_id bigint NOT NULL,
    CONSTRAINT "uk_qa4occ0lq1r2wrxaimcb9or9s" UNIQUE ("transactions_id")
) WITH (oids = false);


DROP TABLE IF EXISTS m_accounts_transaction;
DROP SEQUENCE IF EXISTS m_accounts_transaction_id_seq;
CREATE SEQUENCE m_accounts_transaction_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

CREATE TABLE m_accounts_transaction (
    id integer DEFAULT nextval('m_accounts_transaction_id_seq') NOT NULL,
    event_id character varying(50),
    debit_account_no character varying(20),
    tran_date date NOT NULL,
    credit_account_no character varying(20),
    tran_amount numeric(19,2) NOT NULL,
    payment_reference character varying(255) NOT NULL,
    processed_flg character(1) DEFAULT 'N',
    created_at timestamptz DEFAULT now(),
    tran_id character varying(10),
    CONSTRAINT m_accounts_transaction_pkey PRIMARY KEY (id),
    CONSTRAINT uniquecreditreferencetrandate001 UNIQUE (payment_reference, credit_account_no, tran_date, tran_amount),
    CONSTRAINT uniquedebitreferencetrandate002 UNIQUE (payment_reference, debit_account_no, tran_date, tran_amount),
    CONSTRAINT uniqueeventreferencetrandate003 UNIQUE (payment_reference, event_id, tran_date, tran_amount)
) WITH (oids = false);

TRUNCATE TABLE  m_config CASCADE;
INSERT INTO m_config ("id", "code_name", "del_flg") VALUES
(1,	'Product Type',	'0'),
(2,	'Currency',	'0'),
(3,	'Frequency',	'0'),
(4,	'Interest',	'0'),
(5,	'Batch Account',	'0'),
(6,	'Event',	'0');

TRUNCATE TABLE  m_product_code CASCADE;
INSERT INTO m_product_code ("id", "currency_code", "del_flg", "gl_sub_head_code", "product_code", "product_name", "product_type") VALUES
(1,	'NGN',	'0',	'21200',	'SB601',	'NORMAL SAVINGS INDIVIDUAL',	'SBA'),
(2,	'NGN',	'0',	'21100',	'CA501',	'CURRENT ACCOUNT INDIVIDUAL',	'CAA'),
(3,	'NGN',	'0',	'11104',	'OABAS',	'OAB ASSETS',	'OAB'),
(4,	'NGN',	'0',	'61204',	'OABEX',	'COMMISSION EXPENSE WALLET',	'OAB'),
(5,	'NGN',	'0',	'52306',	'OABIN',	'SMS INCOME CHARGE',	'OAB'),
(6,	'NGN',	'0',	'23102',	'OABLI',	'COLLECTION ACCOUNT AIRTIME',	'OAB'),
(7,	'NGN',	'0',	'21105',	'SB901',	'WALLET COMMISSION',	'SBA');

TRUNCATE TABLE  m_wallet_account_gl CASCADE;
INSERT INTO m_wallet_account_gl ("id", "crncy_code", "del_flg", "entity_cre_flg", "gl_code", "gl_name", "gl_sub_head_code", "sol_id") VALUES
(1,	'NGN',	'0',	'1',	'11001',	'ASSET COLLECTION',	'11001',	'0000');

TRUNCATE TABLE  m_wallet_config CASCADE;
INSERT INTO m_wallet_config ("id", "code_desc", "code_symbol", "code_value", "del_flg", "code_id") VALUES
(1,	'Nigeria',	'Currency',	'NGN',	'0',	2),
(2,	'OAB ASSET',	'Product',	'OAB',	'0',	1),
(3,	'Monthly',	'Frequency',	'M',	'0',	3),
(4,	'ZERO',	'Interest',	'ZEROL',	'0',	4),
(5,	'TELLER',	'ADMIN 1',	'11001001',	'0',	5),
(6,	'TELLER',	'ADMIN 2',	'11002001',	'0',	5),
(7,	'TELLER',	'ADMIN 3',	'11003001',	'0',	5),
(8,	'TELLER',	'ADMIN 4',	'11004001',	'0',	5),
(9,	'TELLER',	'ADMIN 5',	'11005001',	'0',	5),
(10,	'Event',	'EVENT 1',	'12001001',	'0',	5),
(11,	'Event',	'EVENT 2',	'12002001',	'0',	5),
(12,	'Event',	'EVENT 3',	'12003001',	'0',	5),
(13,	'Event',	'EVENT 4',	'12004001',	'0',	5),
(14,	'Event',	'EVENT 5',	'12005001',	'0',	5),
(15,	'Event',	'EVENT 6',	'12006001',	'0',	5),
(16,	'Event',	'EVENT 7',	'12007001',	'0',	5),
(17,	'Event',	'EVENT 8',	'12008001',	'0',	5),
(18,	'Event',	'EVENT 9',	'12009001',	'0',	5),
(19,	'Event',	'EVENT 10',	'12010001',	'0',	5),
(20,	'Event',	'EVENT 11',	'12010002',	'0',	5),
(21,	'United State',	'$',	'USD',	'0',	2),
(22,	'SOCIAL EVENT',	'WAYAGRAM',	'17001001',	'0',	5),
(23,	'SOCIAL EVENT',	'WAYAGRAM',	'17001002',	'0',	5),
(24,	'SOCIAL EVENT',	'WAYAGRAM',	'17001003',	'0',	5),
(25,	'SOCIAL EVENT',	'WAYAGRAM',	'17001004',	'0',	5),
(26,	'SOCIAL EVENT',	'WAYAGRAM',	'17001005',	'0',	5),
(27,	'SOCIAL EVENT',	'WAYAGRAM',	'17001006',	'0',	5),
(28,	'SOCIAL EVENT',	'WAYAGRAM',	'17001007',	'0',	5),
(29,	'SOCIAL EVENT',	'WAYAGRAM',	'17001008',	'0',	5),
(30,	'SOCIAL EVENT',	'WAYAGRAM',	'17001009',	'0',	5);

TRUNCATE TABLE  m_wallet_event CASCADE;
INSERT INTO m_wallet_event ("id", "charge_customer", "charge_waya", "crncy_code", "del_flg", "event_id", "placeholder", "tax_amt", "taxable", "tran_amt", "tran_narration") VALUES
(1,	'1',	'0',	'NGN',	'0',	'BANKPMT',	'12002001',	0.00,	'0',	0.00,	'BANK PAYMENT'),
(2,	'1',	'0',	'NGN',	'0',	'NONWAYAPT',	'12001001',	0.00,	'0',	0.00,	'NON-WAYA PAYMENT'),
(3,	'1',	'0',	'NGN',	'0',	'SMSCHG',	'12003001',	0.00,	'0',	0.00,	'SMS CHARGE FEE'),
(4,	'1',	'0',	'NGN',	'0',	'AITCOL',	'12004001',	0.00,	'0',	0.00,	'AIRTIME COLLECTION'),
(5,	'1',	'0',	'NGN',	'0',	'COMPAYM',	'12005001',	0.00,	'0',	0.00,	'COMPAYM COLLECTION'),
(6,	'1',	'0',	'NGN',	'0',	'COMMPMT',	'12006001',	0.00,	'0',	0.00,	'COMMISSION PAYMENT'),
(7,	'0',	'1',	'NGN',	'0',	'PAYSTK',	'12007001',	0.00,	'0',	0.00,	'PAYSTACK PAYMENT'),
(8,	'0',	'1',	'NGN',	'0',	'PAYSTACK',	'12008001',	0.00,	'0',	0.00,	'PAYSTACK PAYMENT 2'),
(9,	'0',	'1',	'NGN',	'0',	'WEMABK',	'12009001',	0.00,	'0',	0.00,	'WEMA BANK PAYMENT'),
(10,	'1',	'0',	'NGN',	'0',	'WAYAGRAM_DONATION_CR_WAYA_EVT_TRNX_KEY',	'12010001',	0.00,	'0',	0.00,	'WAYAGRAM DONATION'),
(11,	'0',	'1',	'NGN',	'0',	'OPAYCO',	'12010002',	0.00,	'1',	0.00,	'OPAY COLLECTION');

TRUNCATE TABLE   m_wallet_interest CASCADE;
INSERT INTO m_wallet_interest ("id", "begin_slab_amt", "crncy_code", "del_flg", "end_slab_amt", "entity_cre_flg", "int_tbl_code", "int_rate_pcnt", "int_slab_drcr", "int_version_num", "penal_int_pcnt", "penal_portion_ind") VALUES
(1,	0,	'NGN',	'0',	1000000,	'1',	'ZEROL',	1,	'C',	'00001',	0.2,	'F');

TRUNCATE TABLE   m_wallet_product CASCADE;
INSERT INTO m_wallet_product ("id", "cash_cr_limit", "cash_dr_limit", "chq_book_flg", "comm_paid_bacid", "comm_paid_flg", "crncy_code", "del_flg", "gl_sub_head_code", "int_coll_bacid", "int_coll_flg", "int_freq_type_cr", "int_paid_bacid", "int_paid_flg", "int_tbl_code", "mic_event_code", "min_avg_bal", "product_code", "product_desc", "product_min_bal", "product_type", "rcre_time", "rcre_user_id", "staff_product_flg", "sys_gen_acct_flg", "xfer_cr_limit", "xfer_dr_limit") VALUES
(1,	9999999999.99,	9999999999.99,	'0',	'',	'1',	'NGN',	'0',	'21200',	'',	'0',	'M',	'',	'1',	'ZEROL',	'',	'0',	'SB601',	'NORMAL SAVINGS INDIVIDUAL',	0,	'SBA',	'2021-11-23',	'MGR',	'0',	'1',	9999999999.99,	9999999999.99),
(2,	9999999999.99,	9999999999.99,	'0',	'',	'1',	'NGN',	'0',	'21100',	'',	'0',	'M',	'',	'1',	'ZEROL',	'',	'0',	'CA501',	'CURRENT ACCOUNT INDIVIDUAL',	0,	'CAA',	'2021-11-23',	'MGR',	'0',	'1',	9999999999.99,	9999999999.99),
(3,	9999999999.99,	9999999999.99,	'0',	'',	'1',	'NGN',	'0',	'21105',	'',	'0',	'M',	'',	'1',	'ZEROL',	'',	'0',	'SB901',	'WALLET COMMISSION',	0,	'SBA',	'2021-11-23',	'MGR',	'0',	'1',	9999999999.99,	9999999999.99),
(4,	9999999999.99,	9999999999.99,	'0',	'',	'1',	'NGN',	'0',	'11104',	'',	'0',	'M',	'',	'1',	'ZEROL',	'',	'0',	'OABAS',	'OAB ASSETS',	0,	'OAB',	'2021-11-23',	'MGR',	'0',	'1',	9999999999.99,	9999999999.99),
(5,	9999999999.99,	9999999999.99,	'0',	'',	'1',	'NGN',	'0',	'61204',	'',	'0',	'M',	'',	'1',	'ZEROL',	'',	'0',	'OABEX',	'COMMISSION EXPENSE WALLET',	0,	'OAB',	'2021-11-23',	'MGR',	'0',	'1',	9999999999.99,	9999999999.99),
(6,	9999999999.99,	9999999999.99,	'0',	'',	'1',	'NGN',	'0',	'52306',	'',	'0',	'M',	'',	'1',	'ZEROL',	'',	'0',	'OABIN',	'INCOME CHARGE',	0,	'OAB',	'2021-11-23',	'MGR',	'0',	'1',	9999999999.99,	9999999999.99),
(7,	9999999999.99,	9999999999.99,	'0',	'',	'1',	'NGN',	'0',	'23102',	'',	'0',	'M',	'',	'1',	'ZEROL',	'',	'0',	'OABLI',	'COLLECTION ACCOUNT',	0,	'OAB',	'2021-11-23',	'MGR',	'0',	'1',	9999999999.99,	9999999999.99);

TRUNCATE TABLE   m_wallet_switch CASCADE;
INSERT INTO m_wallet_switch ("id", "created_switch_time", "is_switched", "last_switch_time", "switch_code", "switch_code_time", "switch_identity") VALUES
(1,	'2022-01-25 16:15:36.962859',	'0',	NULL,	'Temporal',	NULL,	'Wallet'),
(2,	'2022-01-25 16:16:05.547397',	'1',	NULL,	'Main',	'2022-01-25 16:20:04.955873',	'Wallet');

