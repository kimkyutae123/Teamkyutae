CREATE DATABASE Member;
CREATE TABLE MemberID (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(20) NOT NULL
);
Create Table groupcreate (
  Group_id INT AUTO_INCREMENT PRIMAARY KEY,
  Group_name VARCHAR(20) NOT NULL,
  foreign key (id) references MemberID(user_id)
  );
  Create table GroupAgree(
    Agree_ID INT AUTO_INCREMENT PRIMARY KEY,
    status BOOLEAN
    );
  Create table GroupChat(
    Room_id INT NOT NULL PRIMARY KEY,
    GroupRoom_user INT NOT NULL,
    Message_text VARCHAR(100),
    send_id INT NOT NULL,
    foreign key (Group_id) references Group_id(groupcreate),
    foreign key (id) references user_id(MemberID)
    );



USE Member;
select *from MemberID;
drop table MemberID;
INSERT INTO MemberID VALUE(12314,19,'kendm1@naver.com');
