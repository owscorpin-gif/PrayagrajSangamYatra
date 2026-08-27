-- ====================================================================
-- PRAYAGRAJ YATRA - SUPABASE POSTGRESQL + POSTGIS DATABASE SCHEMA (PHASE 1)
-- ====================================================================
-- Description: Complete production-ready SQL script for Supabase SQL Editor.
-- Features: PostGIS spatial tables, GIST indexes, RLS policies, RPC functions & Seed Data.
-- ====================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- 2. CUSTOM ENUMS
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('pilgrim', 'panda', 'driver', 'admin');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE vehicle_type AS ENUM ('ebike', 'erickshaw', 'auto', 'traveller', 'boat');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE booking_status AS ENUM ('pending', 'confirmed', 'in_progress', 'completed', 'cancelled');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE payment_status AS ENUM ('pending', 'paid', 'refunded', 'failed');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE ritual_category AS ENUM ('snan', 'pind_daan', 'ganga_aarti', 'sankalp', 'havan', 'katha', 'darshan');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE stay_type AS ENUM ('dharamshala', 'ashram', 'hotel', 'homestay', 'tent_city');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE accessibility_level AS ENUM ('easy', 'moderate', 'difficult', 'wheelchair_friendly');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. TABLES

-- 3.1 USERS TABLE (Linked to Supabase Auth)
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    phone_number VARCHAR(20) UNIQUE,
    full_name TEXT,
    email TEXT UNIQUE,
    role user_role DEFAULT 'pilgrim' NOT NULL,
    language_preference VARCHAR(10) DEFAULT 'hi' NOT NULL,
    avatar_url TEXT,
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 3.2 PANDA / PRIEST PROFILES
CREATE TABLE IF NOT EXISTS public.panda_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    accreditation_number VARCHAR(100) UNIQUE,
    languages TEXT[] DEFAULT ARRAY['hi', 'en'],
    bio TEXT,
    years_of_experience INTEGER DEFAULT 1,
    rating NUMERIC(3, 2) DEFAULT 5.00 CHECK (rating >= 1.0 AND rating <= 5.0),
    total_reviews INTEGER DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,
    specializations TEXT[] DEFAULT ARRAY['Sangam Snan', 'Pind Daan', 'Ganga Aarti'],
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 3.3 DRIVER PROFILES (With Live PostGIS Location)
CREATE TABLE IF NOT EXISTS public.driver_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    vehicle_type vehicle_type NOT NULL,
    vehicle_number VARCHAR(50) NOT NULL UNIQUE,
    location GEOGRAPHY(POINT, 4326),
    is_available BOOLEAN DEFAULT TRUE NOT NULL,
    current_status VARCHAR(50) DEFAULT 'online',
    rating NUMERIC(3, 2) DEFAULT 5.00,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 3.4 PLACES (Master Prayagraj Places with PostGIS Point & Accessibility)
CREATE TABLE IF NOT EXISTS public.places (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    hindi_name TEXT,
    category VARCHAR(50) NOT NULL, -- 'ghat', 'temple', 'heritage', 'monument'
    description TEXT,
    location GEOGRAPHY(POINT, 4326) NOT NULL,
    latitude DOUBLE PRECISION GENERATED ALWAYS AS (ST_Y(location::geometry)) STORED,
    longitude DOUBLE PRECISION GENERATED ALWAYS AS (ST_X(location::geometry)) STORED,
    step_count INTEGER DEFAULT 0,
    accessibility_level accessibility_level DEFAULT 'easy' NOT NULL,
    opening_hours TEXT,
    entry_fee NUMERIC(10, 2) DEFAULT 0.00,
    image_url TEXT,
    featured BOOLEAN DEFAULT FALSE,
    tags TEXT[] DEFAULT ARRAY[]::TEXT[],
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 3.5 RITUAL SERVICES
CREATE TABLE IF NOT EXISTS public.ritual_services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    panda_id UUID NOT NULL REFERENCES public.panda_profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    category ritual_category NOT NULL,
    description TEXT,
    dakshina_amount NUMERIC(10, 2) NOT NULL CHECK (dakshina_amount >= 0),
    samagri_included BOOLEAN DEFAULT TRUE NOT NULL,
    boat_included BOOLEAN DEFAULT FALSE NOT NULL,
    duration_minutes INTEGER DEFAULT 45,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 3.6 ACCOMMODATIONS (Dharamshalas, Ashrams, Hotels)
CREATE TABLE IF NOT EXISTS public.accommodations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    stay_type stay_type DEFAULT 'dharamshala' NOT NULL,
    address TEXT NOT NULL,
    location GEOGRAPHY(POINT, 4326) NOT NULL,
    latitude DOUBLE PRECISION GENERATED ALWAYS AS (ST_Y(location::geometry)) STORED,
    longitude DOUBLE PRECISION GENERATED ALWAYS AS (ST_X(location::geometry)) STORED,
    price_per_night NUMERIC(10, 2) NOT NULL,
    amenities TEXT[] DEFAULT ARRAY['Satvik Bhojan', 'Hot Water', 'Ganga View'],
    contact_number VARCHAR(20),
    rating NUMERIC(3, 2) DEFAULT 4.5,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 3.7 ITINERARY CARTS & CART WAYPOINTS
CREATE TABLE IF NOT EXISTS public.itinerary_carts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pilgrim_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title TEXT DEFAULT 'My Prayagraj Yatra',
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.cart_waypoints (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL REFERENCES public.itinerary_carts(id) ON DELETE CASCADE,
    place_id UUID NOT NULL REFERENCES public.places(id) ON DELETE CASCADE,
    stop_order INTEGER NOT NULL,
    planned_duration_minutes INTEGER DEFAULT 60,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    UNIQUE(cart_id, stop_order)
);

-- 3.8 MASTER BOOKINGS & LINE ITEMS
CREATE TABLE IF NOT EXISTS public.bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_code VARCHAR(20) UNIQUE NOT NULL,
    pilgrim_id UUID NOT NULL REFERENCES public.users(id) ON DELETE RESTRICT,
    total_amount NUMERIC(10, 2) NOT NULL CHECK (total_amount >= 0),
    booking_status booking_status DEFAULT 'pending' NOT NULL,
    payment_status payment_status DEFAULT 'pending' NOT NULL,
    booking_date DATE NOT NULL DEFAULT CURRENT_DATE,
    special_instructions TEXT,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.booking_line_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    item_type VARCHAR(50) NOT NULL, -- 'ritual', 'ride', 'stay'
    reference_id UUID NOT NULL,
    quantity INTEGER DEFAULT 1 NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    subtotal_price NUMERIC(10, 2) NOT NULL
);

-- 4. PERFORMANCE SPATIAL & RELATIONAL INDEXES
CREATE INDEX IF NOT EXISTS idx_places_spatial_location ON public.places USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_driver_spatial_location ON public.driver_profiles USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_accommodations_spatial_location ON public.accommodations USING GIST (location);

CREATE INDEX IF NOT EXISTS idx_places_category ON public.places(category);
CREATE INDEX IF NOT EXISTS idx_bookings_pilgrim_id ON public.bookings(pilgrim_id);
CREATE INDEX IF NOT EXISTS idx_cart_waypoints_cart_id ON public.cart_waypoints(cart_id);
CREATE INDEX IF NOT EXISTS idx_ritual_services_panda_id ON public.ritual_services(panda_id);

-- 5. ROW LEVEL SECURITY (RLS) POLICIES
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.panda_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.driver_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.places ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ritual_services ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.accommodations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.itinerary_carts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cart_waypoints ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.booking_line_items ENABLE ROW LEVEL SECURITY;

-- 5.1 Public Read Catalog Data (Places, Rituals, Accommodations)
CREATE POLICY "Public places are readable by all authenticated and anon users"
    ON public.places FOR SELECT USING (true);

CREATE POLICY "Public ritual offerings are readable by all"
    ON public.ritual_services FOR SELECT USING (true);

CREATE POLICY "Public accommodations are readable by all"
    ON public.accommodations FOR SELECT USING (true);

CREATE POLICY "Verified panda profiles are viewable by public"
    ON public.panda_profiles FOR SELECT USING (true);

CREATE POLICY "Active drivers viewable by public"
    ON public.driver_profiles FOR SELECT USING (is_available = true);

-- 5.2 User & Profile Ownership Policies
CREATE POLICY "Users can view and update own profile"
    ON public.users FOR ALL
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Pandas can manage own profile"
    ON public.panda_profiles FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Drivers can update own profile and GPS location"
    ON public.driver_profiles FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- 5.3 Itinerary Cart Policies
CREATE POLICY "Pilgrims can manage own itinerary carts"
    ON public.itinerary_carts FOR ALL
    USING (auth.uid() = pilgrim_id)
    WITH CHECK (auth.uid() = pilgrim_id);

CREATE POLICY "Pilgrims can manage waypoints in own carts"
    ON public.cart_waypoints FOR ALL
    USING (EXISTS (
        SELECT 1 FROM public.itinerary_carts c 
        WHERE c.id = cart_waypoints.cart_id AND c.pilgrim_id = auth.uid()
    ))
    WITH CHECK (EXISTS (
        SELECT 1 FROM public.itinerary_carts c 
        WHERE c.id = cart_waypoints.cart_id AND c.pilgrim_id = auth.uid()
    ));

-- 5.4 Strict Owner-Only Access for Bookings
CREATE POLICY "Pilgrims can view and create own bookings"
    ON public.bookings FOR ALL
    USING (auth.uid() = pilgrim_id)
    WITH CHECK (auth.uid() = pilgrim_id);

CREATE POLICY "Pilgrims can view line items of own bookings"
    ON public.booking_line_items FOR SELECT
    USING (EXISTS (
        SELECT 1 FROM public.bookings b 
        WHERE b.id = booking_line_items.booking_id AND b.pilgrim_id = auth.uid()
    ));

-- 6. POSTGIS STORED RPC FUNCTION: nearby_places
-- Accepts lat, long, and radius_meters, returns nearby places sorted by distance.
CREATE OR REPLACE FUNCTION public.nearby_places(
    lat DOUBLE PRECISION,
    long DOUBLE PRECISION,
    radius_meters DOUBLE PRECISION DEFAULT 5000.0
)
RETURNS TABLE (
    id UUID,
    name TEXT,
    hindi_name TEXT,
    category VARCHAR(50),
    description TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    step_count INTEGER,
    accessibility_level accessibility_level,
    opening_hours TEXT,
    image_url TEXT,
    distance_meters DOUBLE PRECISION
)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    user_location GEOGRAPHY;
BEGIN
    user_location := ST_SetSRID(ST_MakePoint(long, lat), 4326)::geography;

    RETURN QUERY
    SELECT 
        p.id,
        p.name,
        p.hindi_name,
        p.category,
        p.description,
        p.latitude,
        p.longitude,
        p.step_count,
        p.accessibility_level,
        p.opening_hours,
        p.image_url,
        ST_Distance(p.location, user_location) AS distance_meters
    FROM public.places p
    WHERE ST_DWithin(p.location, user_location, radius_meters)
    ORDER BY ST_Distance(p.location, user_location) ASC;
END;
$$;

-- 7. PRAYAGRAJ SEED DATA (Master Places, Services, & Demo Catalog)
INSERT INTO public.places (id, name, hindi_name, category, description, location, step_count, accessibility_level, opening_hours, entry_fee, featured, tags)
VALUES
(
    '11111111-1111-1111-1111-111111111101',
    'Triveni Sangam',
    'त्रिवेणी संगम',
    'ghat',
    'The holy confluence of Ganga, Yamuna, and Saraswati rivers. Sacred site for Kumbh Mela and holy Snan.',
    ST_SetSRID(ST_MakePoint(81.8845, 25.4290), 4326)::geography,
    12,
    'easy',
    'Open 24 Hours',
    0.00,
    true,
    ARRAY['Holy Dip', 'Boat Ride', 'Aarti', 'Spiritual']
),
(
    '11111111-1111-1111-1111-111111111102',
    'Bade Hanuman Ji Temple',
    'बड़े हनुमान जी (लेटे हनुमान)',
    'temple',
    'Ancient underground shrine featuring a 20-foot reclining idol of Lord Hanuman, submerged every year during monsoon floods.',
    ST_SetSRID(ST_MakePoint(81.8790, 25.4325), 4326)::geography,
    18,
    'moderate',
    '05:00 AM - 10:00 PM',
    0.00,
    true,
    ARRAY['Reclining Idol', 'Sindoor Arpan', 'Ancient']
),
(
    '11111111-1111-1111-1111-111111111103',
    'Alopi Devi Shaktipeeth',
    'अलोपी देवी शक्तिपीठ',
    'temple',
    'Revered 51 Shaktipeeth where the wooden Doli (palanquin) of Goddess Sati is venerated.',
    ST_SetSRID(ST_MakePoint(81.8680, 25.4412), 4326)::geography,
    8,
    'wheelchair_friendly',
    '06:00 AM - 09:30 PM',
    0.00,
    true,
    ARRAY['Shaktipeeth', 'Navratri Special', 'Palanquin']
),
(
    '11111111-1111-1111-1111-111111111104',
    'Akshayavat & Patalpuri Temple',
    'अक्षयवट एवं पातालपुरी',
    'temple',
    'The indestructible sacred banyan tree inside the historic Allahabad Fort complex.',
    ST_SetSRID(ST_MakePoint(81.8765, 25.4302), 4326)::geography,
    24,
    'moderate',
    '07:00 AM - 05:00 PM',
    0.00,
    true,
    ARRAY['Immortal Banyan Tree', 'Fort Complex', 'Vedic']
),
(
    '11111111-1111-1111-1111-111111111105',
    'Nag Vasuki Temple',
    'नाग वासुकी मंदिर',
    'temple',
    'Historic shrine dedicated to King of Serpents, Vasuki, along Ganga riverbanks in Daraganj.',
    ST_SetSRID(ST_MakePoint(81.8720, 25.4520), 4326)::geography,
    35,
    'moderate',
    '05:30 AM - 08:30 PM',
    0.00,
    false,
    ARRAY['Nag Panchami', 'Daraganj', 'Ancient Shrine']
),
(
    '11111111-1111-1111-1111-111111111106',
    'Anand Bhavan Museum',
    'आनंद भवन संग्रहालय',
    'heritage',
    'Ancestral mansion of the Nehru family showcasing India freedom struggle relics and modern planetarium.',
    ST_SetSRID(ST_MakePoint(81.8592, 25.4578), 4326)::geography,
    10,
    'wheelchair_friendly',
    '09:30 AM - 05:00 PM',
    70.00,
    true,
    ARRAY['Heritage', 'Museum', 'Freedom Movement']
),
(
    '11111111-1111-1111-1111-111111111107',
    'Chandrashekhar Azad Park',
    'चंद्रशेखर आजाद पार्क',
    'heritage',
    'Historic 133-acre memorial park where revolutionary Chandrashekhar Azad fought heroically.',
    ST_SetSRID(ST_MakePoint(81.8480, 25.4540), 4326)::geography,
    0,
    'easy',
    '05:00 AM - 08:00 PM',
    10.00,
    false,
    ARRAY['Azad Memorial', 'Lush Greenery', 'Museum']
),
(
    '11111111-1111-1111-1111-111111111108',
    'Shankar Viman Mandapam',
    'शंकर विमान मंडपम',
    'temple',
    'Magnificent 130-ft South Indian temple tower situated right next to the Triveni Sangam bank.',
    ST_SetSRID(ST_MakePoint(81.8810, 25.4310), 4326)::geography,
    45,
    'difficult',
    '06:00 AM - 08:00 PM',
    0.00,
    true,
    ARRAY['Dravidian Architecture', 'Sangam View']
)
ON CONFLICT (id) DO NOTHING;
