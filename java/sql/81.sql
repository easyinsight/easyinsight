alter table account_credit_card_billing_info drop foreign key account_credit_card_billing_info_ibfk1;
alter table account_credit_card_billing_info add foreign key (account_id) references account(account_id) on delete cascade;