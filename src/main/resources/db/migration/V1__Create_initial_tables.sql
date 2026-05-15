-- User Table without Dependency
CREATE TABLE IF NOT EXISTS public."User"
(
    "userId" text COLLATE pg_catalog."default" NOT NULL,
    email text COLLATE pg_catalog."default",
    name text COLLATE pg_catalog."default",
    password text COLLATE pg_catalog."default",
    "createAt" timestamp with time zone,
    CONSTRAINT "User_pkey" PRIMARY KEY ("userId"),
    CONSTRAINT email UNIQUE (email)
);

-- Event Table with Dependency to User
CREATE TABLE IF NOT EXISTS public."Event"
(
    "eventId" text COLLATE pg_catalog."default" NOT NULL,
    "creatorId" text COLLATE pg_catalog."default" NOT NULL,
    title text COLLATE pg_catalog."default",
    description text COLLATE pg_catalog."default",
    location text COLLATE pg_catalog."default",
    "dateTime" timestamp with time zone,
    "seatQuota" integer,
    "seatsAvailable" integer,
    price numeric(38,2),
    "isActive" boolean,
    "createAt" timestamp with time zone,
    CONSTRAINT "Event_pkey" PRIMARY KEY ("eventId"),
    CONSTRAINT "creatorId" FOREIGN KEY ("creatorId")
        REFERENCES public."User" ("userId") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Booking Table with Dependency to User and Event
CREATE TABLE IF NOT EXISTS public."Booking"
(
    "bookingId" text COLLATE pg_catalog."default" NOT NULL,
    "eventId" text COLLATE pg_catalog."default" NOT NULL,
    "userId" text COLLATE pg_catalog."default" NOT NULL,
    "createAt" timestamp with time zone,
    quantity integer,
    "bookingReference" text COLLATE pg_catalog."default" NOT NULL,
    status text COLLATE pg_catalog."default",
    CONSTRAINT "Booking_pkey" PRIMARY KEY ("bookingId"),
    CONSTRAINT reference UNIQUE ("bookingReference"),
    CONSTRAINT event FOREIGN KEY ("eventId")
        REFERENCES public."Event" ("eventId") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT "user" FOREIGN KEY ("userId")
        REFERENCES public."User" ("userId") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);