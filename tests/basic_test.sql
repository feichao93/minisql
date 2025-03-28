create table student (
	sno char(8),
	sname char(16) unique,
	sage int,
	sgender char(1),
	score float,
	primary key ( sno )
);


delete from student;
insert into student values ('12345678',	'wy1',22,'M',95);
insert into student values ('12345679',	'wy2',19,'F',100);
create index stunameidx on student ( sname );
insert into student values ('12345682',	'wy5',14,'M',60);
insert into student values ('12345684',	'wy6',25,'F',63);
select * from student;
select * from student where sno = '12345679';
select * from student where score >= 90 and score <=95;
select * from student where score > 60 and score <65;
select * from student where score >= 98;
select * from student where sage > 20 and sgender = 'F';
delete from student where sno = '12345678';
delete from student where sname = 'wy2';
select * from student;
insert into student values ('12345681',	'wy4',23,'F',96);
insert into student values ('12345670',	'wy3',25,'M',0);
select * from student where score < 10;
select * from student where sgender <> 'F';