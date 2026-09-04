-- =========================================================================================
-- SUPABASE POSTGRESQL SCHEMA FOR PRAYAGRAJ SANGAM YATRA BOOKING & ACCOMMODATION VERIFICATION
-- =========================================================================================

-- 1. Create Enums for Verification Status and Categories
CREATE TYPE reservation_category_enum AS ENUM (
    'RITUAL_SANKALP',
    'BOAT_CONFLUENCE',
    'TEMPLE_VIP_DARSHAN',
    'TENT_ACCOMMODATION',
    'SPECIAL_PERMIT'
);

CREATE TYPE verification_status_enum AS ENUM (
    'OFFICIALLY_VERIFIED',
    'SUSPICIOUS_UNREGISTERED_FRAUD',
    'FLAGGED_BLACKLISTED_TOUT',
    'EXPIRED',
    'CANCELLED'
);

-- 2. Verified Accommodation Listings Table
CREATE TABLE IF NOT EXISTS verified_accommodations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_code VARCHAR(64) UNIQUE NOT NULL,
    property_name VARCHAR(255) NOT NULL,
    authority_permit_no VARCHAR(128) UNIQUE NOT NULL,
    category VARCHAR(64) DEFAULT 'TENT_ACCOMMODATION',
    provider_operator_name VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    sector_zone VARCHAR(64) NOT NULL,
    location_address TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    govt_capped_rate_per_night DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_blacklisted BOOLEAN DEFAULT FALSE,
    security_clearance_status VARCHAR(64) DEFAULT 'APPROVED',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Master Verified Booking Reservations Table
CREATE TABLE IF NOT EXISTS booking_reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id VARCHAR(64) UNIQUE NOT NULL,
    verification_code VARCHAR(64) UNIQUE NOT NULL,
    registration_certificate_no VARCHAR(128) UNIQUE NOT NULL,
    category reservation_category_enum NOT NULL DEFAULT 'RITUAL_SANKALP',
    title VARCHAR(255) NOT NULL,
    hindi_title VARCHAR(255),
    authority_name VARCHAR(255) DEFAULT 'Prayagraj Mela Pradhikaran & Sangam Shrine Board',
    pilgrim_name VARCHAR(255) NOT NULL,
    pilgrim_phone VARCHAR(32) NOT NULL,
    gotra_or_party VARCHAR(128) DEFAULT 'N/A',
    num_persons INT DEFAULT 1,
    assigned_provider_name VARCHAR(255) NOT NULL,
    provider_badge_no VARCHAR(128) NOT NULL,
    provider_contact VARCHAR(32) NOT NULL,
    service_location VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION DEFAULT 25.4300,
    longitude DOUBLE PRECISION DEFAULT 81.8845,
    slot_date_time VARCHAR(128) NOT NULL,
    fixed_govt_tariff VARCHAR(64) NOT NULL,
    status verification_status_enum NOT NULL DEFAULT 'OFFICIALLY_VERIFIED',
    qr_hash_signature TEXT NOT NULL,
    anti_counterfeit_seal VARCHAR(128) NOT NULL,
    security_notes TEXT,
    is_blacklisted BOOLEAN DEFAULT FALSE,
    blacklist_reason TEXT,
    report_helpline_number VARCHAR(32) DEFAULT '1920',
    includes_life_jacket_or_samagri BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Verification Scan Audit Logs Table
CREATE TABLE IF NOT EXISTS verification_scan_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scanned_query VARCHAR(128) NOT NULL,
    matched_booking_id VARCHAR(64),
    status_returned verification_status_enum NOT NULL,
    scan_source VARCHAR(32) DEFAULT 'ANDROID_APP',
    scanner_ip_or_hash VARCHAR(128),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Indexes for High-Performance Real-Time Lookups
CREATE INDEX IF NOT EXISTS idx_booking_reservations_booking_id ON booking_reservations(booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_reservations_verification_code ON booking_reservations(verification_code);
CREATE INDEX IF NOT EXISTS idx_booking_reservations_certificate_no ON booking_reservations(registration_certificate_no);
CREATE INDEX IF NOT EXISTS idx_verified_accommodations_property_code ON verified_accommodations(property_code);
CREATE INDEX IF NOT EXISTS idx_verification_scan_logs_query ON verification_scan_logs(scanned_query);

-- 6. Row Level Security (RLS) Policies
ALTER TABLE verified_accommodations ENABLE ROW LEVEL SECURITY;
ALTER TABLE booking_reservations ENABLE ROW LEVEL SECURITY;
ALTER TABLE verification_scan_logs ENABLE ROW LEVEL SECURITY;

-- Allow public read access to verify booking validity
CREATE POLICY "Allow public verification lookup on booking_reservations"
    ON booking_reservations FOR SELECT
    USING (true);

CREATE POLICY "Allow public lookup on verified_accommodations"
    ON verified_accommodations FOR SELECT
    USING (is_active = true);

CREATE POLICY "Allow anonymous logging of scans"
    ON verification_scan_logs FOR INSERT
    WITH CHECK (true);

-- 7. SQL Verification Helper Function
CREATE OR REPLACE FUNCTION verify_booking_pass(p_query TEXT)
RETURNS SETOF booking_reservations AS $$
BEGIN
    -- Log the scan audit
    INSERT INTO verification_scan_logs (scanned_query, scan_source)
    VALUES (p_query, 'APP_VERIFY_FUNCTION');

    -- Search across booking_id, verification_code, and registration certificate
    RETURN QUERY
    SELECT *
    FROM booking_reservations
    WHERE UPPER(booking_id) = UPPER(TRIM(p_query))
       OR UPPER(verification_code) = UPPER(TRIM(p_query))
       OR UPPER(registration_certificate_no) = UPPER(TRIM(p_query))
    LIMIT 1;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
