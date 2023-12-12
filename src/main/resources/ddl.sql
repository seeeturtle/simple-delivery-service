

CREATE TABLE `store_category` (
	`category_id`	int	NOT NULL,
	`title`	varchar(10)	NULL,
	`status`	boolean	NULL
);

CREATE TABLE `store_information` (
	`store_id`	int	NOT NULL,
	`name`	varchar(20)	NULL,
	`address`	varchar(100)	NULL,
	`tel`	varchar(14)	NULL,
	`open_hour`	varchar(40)	NULL,
	`close_day`	datetime	NULL,
	`min_cost`	int	NULL,
	`description`	TEXT	NULL,
	`delivery_fee`	int	NULL
);

CREATE TABLE `store_menu` (
	`menu_id`	int	NOT NULL,
	`store_id`	int	NOT NULL,
	`menu_cg_id`	int	NOT NULL,
	`name`	varchar(10)	NULL,
	`price`	int	NULL,
	`origin`	varchar(20)	NULL,
	`description`	varchar(30)	NULL,
	`status`	boolean	NULL
);

CREATE TABLE `order_menu` (
	`order_menu_id`	int	NOT NULL AUTO_INCREMENT,
	`menu_id2`	int	NOT NULL,
	`cart_id`	int	NOT NULL,
	`order_menu_quantity`	int	NULL,
	`price`	int	NULL,
	`status`	boolean	NULL,
    PRIMARY KEY(`order_menu_id`)
);

CREATE TABLE `order` (
	`order_id`	int	NOT NULL,
	`cart_id`	int	NOT NULL,
	`user_id`	int	NOT NULL,
	`store_id`	int	NOT NULL,
	`address_id`	varchar(100)	NOT NULL,
	`coupon_id`	int	NOT NULL,
	`payment_id`	int	NOT NULL,
	`payment_method`	varchar(30)	NULL,
	`to_owner_memo`	varchar(30)	NULL,
	`to_rider_memo`	varchar(30)	NULL,
	`delivery_fee`	int	NULL,
	`total_price`	int	NULL,
	`pay_with_point`	int	NULL,
	`total_pay`	int	NULL,
	`order_status`	varchar(10)	NULL,
	`payment_historyID`	int	NOT NULL,
	`status`	boolean	NULL
);

CREATE TABLE `user` (
	`user_id`	int	NOT NULL,
	`nickname`	varchar(40)	NULL,
	`email`	varchar(40)	NULL,
	`phonenumber`	varchar(40)	NULL,
	`status`	boolean	NULL
);

CREATE TABLE `rider` (
	`rider_id`	int	NOT NULL,
	`rider_nickname`	varchar(40)	NULL,
	`rider_email`	varchar(40)	NULL,
	`rider_phonenumber`	varchar(40)	NULL,
	`status`	boolean	NULL
);

CREATE TABLE `owner` (
	`owner_id`	int	NOT NULL,
	`owner_nickname`	varchar(40)	NULL,
	`owner_email`	varchar(40)	NULL,
	`owner_phonenumber`	varchar(40)	NULL,
	`status`	boolean	NULL
);

CREATE TABLE `menu_category` (
	`menu_cg_id`	int	NOT NULL,
	`title`	varchar(10)	NULL,
	`status`	boolean	NULL
);

CREATE TABLE `store` (
	`store_id`	int	NOT NULL,
	`category_id`	int	NOT NULL,
	`owner_id`	int	NOT NULL,
	`is_ad`	boolean	NULL,
	`status`	boolean	NULL
);

CREATE TABLE `store_review` (
	`store_review_id`	int	NOT NULL,
	`store_id`	int	NOT NULL,
	`rate`	int	NULL,
	`content`	varchar(200)	NULL,
	`status`	boolean	NULL
);

CREATE TABLE `order_cart` (
	`cart_id`	int	NOT NULL AUTO_INCREMENT,
	`store_id`	int	NOT NULL,
	`total_price`	int	NULL,
	`delivery_fee`	int	NULL,
	`status`	boolean	NULL,
    PRIMARY KEY (`cart_id`)
);

INSERT INTO user
VALUES (1, 'user_example', 'qkrwnstjr@cau.ac.kr', '010-1234-1234', 1);

INSERT INTO owner
VALUES (1, 'owner_example', 'owner@cau.ac.kr', '010-5678-5678', 1);

INSERT INTO rider
VALUES (1, 'rider_example', 'rider@cau.ac.kr', '010-7890-7890', 1);

INSERT INTO menu_category
VALUES (1, '인기 메뉴', 1),
       (2, '대표 메뉴', 1),
       (3, '사이드 메뉴', 1),
       (4, '음료 메뉴', 1),
       (5, '주류 메뉴', 1);

INSERT INTO store_category
VALUES (1, '족발·보쌈', 1),
    (2, '찜·탕·찌개', 1),
    (3, '고기·구이', 1),
    (4, '야식', 1),
    (5, '양식', 1),
    (6, '돈까스·회', 1),
    (7, '피자', 1),
    (8, '중식', 1),
    (9, '치킨', 1),
    (10, '버거', 1),
    (11, '분식', 1),
    (12, '디저트', 1),
    (13, '찜·찌개', 1),
    (14, '전체 보기', 1);

INSERT INTO store
VALUES (1, 9, 1, 0, 1),
       (2, 9, 1, 0, 1),
       (3, 9, 1, 0, 1),
       (4, 9, 1, 0, 1),
       (5, 9, 1, 0, 1),
       (6, 9, 1, 0, 1);


INSERT INTO store_information
VALUES (1, '굽네치킨 분당동점', '경기도 성남시 분당구 분당동 96 1층2호일부, 3호', '050-4831-1525', '오후 12:00', '2023-12-12 00:00:00', 15000, '오븐에 구워 더욱 맛있고 담백한 웰빙 굽네치킨 입니다.', 3000),
       (2, '후라이드참잘하는집 수내서현점', '경기도 성남시 분당구 서현동 93 1층 118호', '050-6329-4379', '오전 9:00', '2023-12-12 00:00:00', 12000, '후라이드가 참 맛있는 치킨 후라이드참잘하는집 입니다.', 1000),
       (3, 'a', 'aa', 'aaa', '오전 9:00', '2023-12-12 00:00:00', 13000, 'aaaa', 1000),
       (4, 'b', 'bb', 'bbb', '오전 9:00', '2023-12-12 00:00:00', 13000, 'bbbb', 2000),
       (5, 'c', 'cc', 'ccc', '오전 9:00', '2023-12-12 00:00:00', 13000, 'cccc', 3000),
       (6, 'd', 'dd', 'ddd', '오전 9:00', '2023-12-12 00:00:00', 13000, 'dddd', 4000);


INSERT INTO store_menu
VALUES (1, 1, 1, '고추바사삭', 18000, '국내산', '1초에 한마리 씩 팔리는 국민 치킨', 1),
       (2, 1, 1, '볼케이노', 18000, '국내산', '매운 바베큐 소스의 국가대표 "매운맛치킨"', 1),
       (3, 1, 1, '오리지널', 16000, '국내산', '기름기 쫙 뺀 담백하고 촉촉한 치킨', 1),
       (4, 1, 2, '고추바사삭 곱빼기', 27000, '국내산', '1닭으로 부족한 당신을 위한 1.5닭', 1),
       (5, 1, 2, '굽네베스트 SET', 22000, '국내산', '온가족이 다같이 즐길 수 있는 베스트세트', 1),
       (6, 1, 3, '볼로네제 파스타', 6500, '국내산', '풍미 가득한 볼로네제 파스타', 1),
       (7, 1, 3, '까르보나라 파스타', 7000, '국내산', '크리미한 까르보나라 파스타', 1),
       (8, 1, 4, '제로 펲시 콜라', '2000', '', '', 1),
       (9, 1, 5, '생맥주', '5000', '', '', 1);

INSERT INTO store_review
VALUES (1, 1, 4, "역시 밤에 먹는 치킨은 최고에요", 1),
	   (2, 1, 3, "치킨이 너무 매워요", 1),
       (3, 2, 4, "후라이드가 맛있어요", 1),
       (4, 2, 5, "이곳만 한 데가 없습니다", 1),
       (5, 3, 3, "aaaa", 1),
       (6, 4, 2, "bbbb", 1),
       (7, 5, 1, "cccc", 1),
       (8, 6, 1, "dddd", 1);
