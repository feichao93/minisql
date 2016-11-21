create table student(
name char(10),
sid int,
primary key(sid));


create table teacher(
name char(10), 
tid int,
college char(10),
primary key(tid));


insert into student values("shi", 1);
insert into student values("ni", 2);
insert into student values("ma", 3);


insert into teacher values(
"ShiFeichao", 1, "CS college");

insert into teacher values(
"QianDongle", 2, "Software");

insert into teacher values(
"DingKan", 3, "Engeering");



--- 目前的问题:
---   1)  条件解析的时候字符串解析时 需要先解析两边的双引号 // OK!
---   2)  查询优化 (最麻烦)
---   3)  输出优化    // OK!
---   4)  execfile指令 // OK!
---   5)  LRU算法实现  // 实现了基于时间的随机算法
---   6)  对负数的支持 // OK!
---   7)  delete指令  // OK!
---   8)  修复float转换时的bug // OK!