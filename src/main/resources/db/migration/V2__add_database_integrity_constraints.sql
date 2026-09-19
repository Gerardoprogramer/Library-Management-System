-- ============================================================
-- V2 - Database integrity constraints
-- ============================================================

-- Align database column sizes with API/domain validation

ALTER TABLE public.book
ALTER COLUMN description TYPE varchar(500);

ALTER TABLE public.book_review
ALTER COLUMN review_text TYPE varchar(2000);

ALTER TABLE public.payable
ALTER COLUMN notes TYPE varchar(1000);

ALTER TABLE public.subscription_plan
ALTER COLUMN admin_notes TYPE varchar(500);


-- Required domain values

ALTER TABLE public.fine
    ALTER COLUMN currency SET NOT NULL;

ALTER TABLE public.subscription_plan
    ALTER COLUMN currency SET NOT NULL;


-- Book inventory integrity

ALTER TABLE public.book
    ADD CONSTRAINT chk_book_total_copies_positive
        CHECK (total_copies >= 1),

    ADD CONSTRAINT chk_book_available_copies_non_negative
        CHECK (available_copies >= 0),

    ADD CONSTRAINT chk_book_available_copies_not_exceed_total
        CHECK (available_copies <= total_copies),

    ADD CONSTRAINT chk_book_pages_positive
        CHECK (pages IS NULL OR pages >= 1),

    ADD CONSTRAINT chk_book_price_non_negative
        CHECK (price IS NULL OR price >= 0);


-- Book loan integrity

ALTER TABLE public.book_loan
    ADD CONSTRAINT chk_book_loan_renewal_count_non_negative
        CHECK (renewal_count >= 0),

    ADD CONSTRAINT chk_book_loan_max_renewals_non_negative
        CHECK (max_renewals >= 0),

    ADD CONSTRAINT chk_book_loan_overdue_days_non_negative
        CHECK (overdue_days >= 0),

    ADD CONSTRAINT chk_book_loan_due_after_checkout
        CHECK (due_date >= checkout_date),

    ADD CONSTRAINT chk_book_loan_return_after_checkout
        CHECK (
            return_date IS NULL
            OR return_date >= checkout_date
        );


-- Book review integrity

ALTER TABLE public.book_review
    ADD CONSTRAINT chk_book_review_rating
        CHECK (rating BETWEEN 1 AND 5);

CREATE UNIQUE INDEX ux_book_review_user_book
    ON public.book_review (user_id, book_id);


-- Fine/payment integrity

ALTER TABLE public.fine
    ADD CONSTRAINT chk_fine_amount_positive
        CHECK (amount > 0);

ALTER TABLE public.payment
    ADD CONSTRAINT chk_payment_amount_positive
        CHECK (amount > 0);


-- Subscription plan integrity

ALTER TABLE public.subscription_plan
    ADD CONSTRAINT chk_subscription_plan_duration_positive
        CHECK (duration_days > 0),

    ADD CONSTRAINT chk_subscription_plan_price_positive
        CHECK (price > 0),

    ADD CONSTRAINT chk_subscription_plan_max_books_positive
        CHECK (max_books_allowed > 0),

    ADD CONSTRAINT chk_subscription_plan_max_days_positive
        CHECK (max_days_per_book > 0),

    ADD CONSTRAINT chk_subscription_plan_display_order
        CHECK (
            display_order IS NULL
            OR display_order >= 0
        );


-- Subscription integrity

ALTER TABLE public.subscriptions
    ADD CONSTRAINT chk_subscription_price_positive
        CHECK (price > 0),

    ADD CONSTRAINT chk_subscription_max_books_positive
        CHECK (max_books_allowed > 0),

    ADD CONSTRAINT chk_subscription_max_days_positive
        CHECK (max_days_per_book > 0),

    ADD CONSTRAINT chk_subscription_renewal_attempts_non_negative
        CHECK (
            renewal_attempt_count IS NULL
            OR renewal_attempt_count >= 0
        ),

    ADD CONSTRAINT chk_subscription_valid_period
        CHECK (end_date > start_date);


-- Reservation integrity

ALTER TABLE public.reservation
    ADD CONSTRAINT chk_reservation_queue_position_positive
        CHECK (
            queue_position IS NULL
                OR queue_position > 0
            ),

    ADD CONSTRAINT chk_reservation_available_window
        CHECK (
            status <> 'AVAILABLE'
            OR (
                available_at IS NOT NULL
                AND available_until IS NOT NULL
                AND available_until > available_at
            )
        );


-- Concurrency/business invariants

CREATE UNIQUE INDEX ux_book_loan_active_user_book
    ON public.book_loan (user_id, book_id)
    WHERE status IN ('CHECKED_OUT', 'OVERDUE');

CREATE UNIQUE INDEX ux_reservation_active_user_book
    ON public.reservation (user_id, book_id)
    WHERE status IN ('PENDING', 'AVAILABLE');


-- Reservation lifecycle indexes

CREATE INDEX idx_reservation_book_status_reserved
    ON public.reservation (book_id, status, reserved_at);

CREATE INDEX idx_reservation_status_available_until
    ON public.reservation (status, available_until);