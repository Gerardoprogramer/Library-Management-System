-- ============================================================
-- V3 - Normalize user emails
-- ============================================================

-- Normalize historical values
UPDATE public.users
SET email = lower(btrim(email))
WHERE email <> lower(btrim(email));


-- Prevent case-insensitive duplicates
CREATE UNIQUE INDEX ux_users_email_normalized
    ON public.users (lower(email));


-- Ensure future values are stored normalized
ALTER TABLE public.users
    ADD CONSTRAINT chk_users_email_normalized
        CHECK (email = lower(btrim(email)));