#!/bin/sh

  docker run --rm -it --net=services mysql:5.7 sh -c "mysql -h shop-mysql -e \"DROP DATABASE IF EXISTS shop;\""

docker run --rm -it --net=services mysql:5.7 sh -c "mysql -h shop-mysql -e \"CREATE DATABASE shop CHARACTER SET utf8 COLLATE utf8_bin;\""

docker run --rm -it --net=services mysql:5.7 sh -c "mysql -h shop-mysql -e \"CREATE USER IF NOT EXISTS 'shop' IDENTIFIED WITH mysql_native_password BY 'password' PASSWORD EXPIRE NEVER;\""

docker run --rm -it --net=services mysql:5.7 sh -c "mysql -h shop-mysql -e \"FLUSH PRIVILEGES;\""

docker run --rm -it --net=services mysql:5.7 sh -c "mysql -h shop-mysql -e \"GRANT ALL ON shop.* TO 'shop'@'%';\""

docker run --rm -it --net=services mysql:5.7 sh -c "mysql -h shop-mysql -e \"FLUSH PRIVILEGES;\""

docker run --rm -it --net=services mysql:5.7 sh -c "mysql -h shop-mysql -e \"USE shop; CREATE TABLE IF NOT EXISTS ACCOUNTS (UUID VARCHAR(36) PRIMARY KEY, NAME VARCHAR(1024) NOT NULL, EMAIL VARCHAR(1024) NOT NULL, ROLE VARCHAR(128) NOT NULL);\""

#docker run --rm -it --net=services mysql:5.7 sh -c "mysqladmin -u root password 'password'"
