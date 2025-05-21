CREATE DATABASE Member;
CREATE TABLE MemberID (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  userage VARCHAR(20) NOT NULL,
  user_email varchar(20) NOT NULL
);

USE Member;
select *from MemberID;
drop table MemberID;
INSERT INTO MemberID VALUE(12314,19,'kendm1@naver.com');
