-- 1. SEED DATA USER
-- Email : test1@gmail.com Password : test1234
-- Email : test2@gmail.com Password : test1234
-- Email : ticketbuyer@gmail.com Password : ijustbuyaticket
INSERT INTO public."User" ("userId", email, name, password, "createAt")
VALUES
    ('9dc6d9f5-bba4-4945-bbdb-f4fe8b974ea3', 'test2@gmail.com', 'Testing 2', '$2a$10$K2iUI6IGFvA86lfE8yVvKu2V3qTMz1BkPVJ2OFgA8EB0Ic5lgaQga', '2026-05-16 01:43:06.498321+07'),
    ('a6383fbe-8e4c-43d2-b0b3-c355a1189e72', 'ticketbuyer@gmail.com', 'Ticket Buyer Tester', '$2a$10$tdo1nsnTXDM70zrJO803Cezd462qQVT0PoMmS9HJ.c9hzqOCBxRdy', '2026-05-16 01:43:40.727152+07'),
    ('c7e76a0f-627e-4b2b-994d-5da7b10bd063', 'test1@gmail.com', 'Testing 1', '$2a$10$LCxssoisaGjkqauqGHdVmOrE.lEBa28TT5a8gbWeBhw0XUIPiFHJ.', '2026-05-16 01:40:34.180427+07');


-- 2. SEED DATA EVENT
INSERT INTO public."Event" ("eventId", "creatorId", title, description, location, "dateTime", "seatQuota", "seatsAvailable", price, "isActive", "createAt")
VALUES
    ('35c073fa-64eb-4c48-ad1d-5310605dfda1', '9dc6d9f5-bba4-4945-bbdb-f4fe8b974ea3', 'Concert Open The Sky by Ilham', 'You want to shake your  head?', 'Jakarta', '2026-05-30 19:00:00+07', 100, 100, 150000.00, true, '2026-05-16 01:52:14.615561+07'),
    ('7b01fadb-9c7e-43d1-b30b-43e6dfca5d7a', '9dc6d9f5-bba4-4945-bbdb-f4fe8b974ea3', 'Webinar Menjadi Manusia', 'Merasa bukan manusia? Mau jadi manusia? Beli!!', 'Depok', '2026-07-30 23:00:00+07', 20, 20, 30000.00, true, '2026-05-16 01:53:34.588506+07'),
    ('9c38b8ba-b319-41d2-9ae5-b045a0d6af11', 'c7e76a0f-627e-4b2b-994d-5da7b10bd063', 'Webinar - First Event Ever', 'The first event input here so, please booking the tickets!!', 'Bandung', '2026-06-16 13:00:00+07', 50, 50, 50000.00, true, '2026-05-16 01:50:06.487747+07');


-- 3. SEED DATA BOOKING
INSERT INTO public."Booking" ("bookingId", "eventId", "userId", "createAt", quantity, "bookingReference", status)
VALUES
    ('5c2058fa-271a-47db-b100-c872a35aeef3', '7b01fadb-9c7e-43d1-b30b-43e6dfca5d7a', 'a6383fbe-8e4c-43d2-b0b3-c355a1189e72', '2026-05-16 02:01:11.483609+07', 10, 'BKG-260516020111-4E73F1', 'CONFIRMED');