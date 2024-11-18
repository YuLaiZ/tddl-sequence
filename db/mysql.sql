-- create table
drop table if exists sequence;
create table sequence
(
    `name`         varchar(255) comment '序列名',
    `value`        bigint comment '序列值',
    `gmt_modified` timestamp comment '最后更新时间',
    `step`         int comment '步长',
    primary key (`name`)
) comment = '序列名';

-- test data
insert into sequence(`name`, `value`, `gmt_modified`, `step`)
values ('tddl_test_seq',  10000000, now(), 10000),
       ('tddl_test_seq1', 50000000, now(), 10000);