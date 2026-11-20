insert into member (name, email, created_at, updated_at)
values ('기본 회원', 'member@example.com', current_timestamp, current_timestamp);

insert into category (name, created_at, updated_at) values ('음악', current_timestamp, current_timestamp);
insert into category (name, created_at, updated_at) values ('영상', current_timestamp, current_timestamp);
insert into category (name, created_at, updated_at) values ('생산성', current_timestamp, current_timestamp);

-- 구독 더미 데이터 (member_id 1: 기본 회원)
insert into sub (member_id, category_id, service_name, monthly_price, billing_day, start_date, active, created_at, updated_at)
values (1, 1, '유튜브 뮤직', 14900, 1, '2026-08-01', true, current_timestamp, current_timestamp);
insert into sub (member_id, category_id, service_name, monthly_price, billing_day, start_date, active, created_at, updated_at)
values (1, 2, '넷플릭스', 17000, 25, '2026-06-25', true, current_timestamp, current_timestamp);
insert into sub (member_id, category_id, service_name, monthly_price, billing_day, start_date, active, created_at, updated_at)
values (1, 3, '노션', 12000, 31, '2026-09-30', true, current_timestamp, current_timestamp);
insert into sub (member_id, category_id, service_name, monthly_price, billing_day, start_date, active, created_at, updated_at)
values (1, 2, '왓챠', 7900, 15, '2026-03-15', false, current_timestamp, current_timestamp);
