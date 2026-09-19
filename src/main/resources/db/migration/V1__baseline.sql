--
-- PostgreSQL database dump
--


-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: book; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.book (
    active boolean NOT NULL,
    available_copies integer NOT NULL,
    pages integer,
    price numeric(10,2),
    published_date date,
    total_copies integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    genre_id uuid NOT NULL,
    id uuid NOT NULL,
    author character varying(255) NOT NULL,
    cover_image_url character varying(255),
    description character varying(255),
    isbn character varying(255) NOT NULL,
    language character varying(255),
    publisher character varying(255),
    title character varying(255) NOT NULL
);


--
-- Name: book_loan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.book_loan (
    max_renewals integer NOT NULL,
    overdue boolean NOT NULL,
    overdue_days integer NOT NULL,
    renewal_count integer NOT NULL,
    checkout_date timestamp(6) without time zone NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    due_date timestamp(6) without time zone NOT NULL,
    return_date timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    book_id uuid NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    type character varying(20) NOT NULL,
    notes character varying(500),
    CONSTRAINT book_loan_status_check CHECK (((status)::text = ANY ((ARRAY['CHECKED_OUT'::character varying, 'RETURNED'::character varying, 'OVERDUE'::character varying, 'LOST'::character varying, 'DAMAGED'::character varying])::text[]))),
    CONSTRAINT book_loan_type_check CHECK (((type)::text = ANY ((ARRAY['CHECKOUT'::character varying, 'RENEWAL'::character varying, 'RETURN'::character varying])::text[])))
);


--
-- Name: book_review; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.book_review (
    rating integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    book_id uuid NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    review_text character varying(255) NOT NULL,
    title character varying(255)
);


--
-- Name: fine; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fine (
    amount numeric(10,2) NOT NULL,
    paid_at timestamp(6) without time zone,
    waived_at timestamp(6) without time zone,
    book_loan_id uuid NOT NULL,
    id uuid NOT NULL,
    processed_by_user_id uuid,
    waived_by_id uuid,
    transaction_id character varying(100),
    reason character varying(500),
    waiver_reason character varying(500),
    currency character varying(255),
    status character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    CONSTRAINT fine_currency_check CHECK (((currency)::text = ANY ((ARRAY['USD'::character varying, 'CRC'::character varying, 'EUR'::character varying])::text[]))),
    CONSTRAINT fine_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PARTIALLY_PAID'::character varying, 'PAID'::character varying, 'WAIVED'::character varying])::text[]))),
    CONSTRAINT fine_type_check CHECK (((type)::text = ANY ((ARRAY['OVERDUE'::character varying, 'DAMAGE'::character varying, 'LOSS'::character varying, 'PROCESSING'::character varying])::text[])))
);


--
-- Name: genre; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.genre (
    active boolean NOT NULL,
    display_order integer NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    id uuid NOT NULL,
    parent_genre_id uuid,
    description character varying(500),
    code character varying(255) NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_reset_tokens (
    expiry_date timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token character varying(255) NOT NULL
);


--
-- Name: payable; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payable (
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    notes character varying(500)
);


--
-- Name: payment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment (
    amount numeric(19,4) NOT NULL,
    renewal_payment boolean NOT NULL,
    completed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone,
    initiated_at timestamp(6) without time zone,
    refunded_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    id uuid NOT NULL,
    payable_id uuid,
    user_id uuid NOT NULL,
    charge_id character varying(255),
    checkout_session_id character varying(255),
    currency character varying(255) NOT NULL,
    description character varying(255),
    failure_reason character varying(255),
    payment_gateway character varying(255),
    payment_intent_id character varying(255),
    payment_status character varying(255) NOT NULL,
    payment_type character varying(255) NOT NULL,
    refund_id character varying(255),
    transaction_id character varying(255),
    CONSTRAINT payment_currency_check CHECK (((currency)::text = ANY ((ARRAY['USD'::character varying, 'CRC'::character varying, 'EUR'::character varying])::text[]))),
    CONSTRAINT payment_payment_gateway_check CHECK (((payment_gateway)::text = 'STRIPE'::text)),
    CONSTRAINT payment_payment_status_check CHECK (((payment_status)::text = ANY ((ARRAY['PENDING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying, 'REFUNDED'::character varying])::text[]))),
    CONSTRAINT payment_payment_type_check CHECK (((payment_type)::text = ANY ((ARRAY['FINE'::character varying, 'MEMBERSHIP'::character varying, 'LOST_BOOK_PENALTY'::character varying, 'DAMAGED_BOOK_PENALTY'::character varying, 'REFUND'::character varying])::text[])))
);


--
-- Name: refresh_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_token (
    expiry_date timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token character varying(255) NOT NULL
);


--
-- Name: reservation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reservation (
    notification_sent boolean NOT NULL,
    queue_position integer,
    available_at timestamp(6) without time zone,
    available_until timestamp(6) without time zone,
    cancelled_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    fulfilled_at timestamp(6) without time zone,
    reserved_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    book_id uuid NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    notes character varying(500),
    status character varying(255) NOT NULL,
    CONSTRAINT reservation_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'AVAILABLE'::character varying, 'FULFILLED'::character varying, 'CANCELLED'::character varying, 'EXPIRED'::character varying])::text[])))
);


--
-- Name: stripe_webhook_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stripe_webhook_events (
    processed_at timestamp(6) without time zone,
    event_id character varying(255) NOT NULL,
    event_type character varying(255)
);


--
-- Name: subscription_plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subscription_plan (
    active boolean,
    display_order integer,
    duration_days integer NOT NULL,
    featured boolean,
    max_books_allowed integer NOT NULL,
    max_days_per_book integer NOT NULL,
    created_at timestamp(6) without time zone,
    price bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    id uuid NOT NULL,
    name character varying(100) NOT NULL,
    admin_notes character varying(255),
    badge_text character varying(255),
    created_by character varying(255),
    currency character varying(255),
    description character varying(255),
    plan_code character varying(255) NOT NULL,
    updated_by character varying(255),
    CONSTRAINT subscription_plan_currency_check CHECK (((currency)::text = ANY ((ARRAY['USD'::character varying, 'CRC'::character varying, 'EUR'::character varying])::text[])))
);


--
-- Name: subscriptions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subscriptions (
    active boolean NOT NULL,
    auto_renew boolean NOT NULL,
    max_books_allowed integer NOT NULL,
    max_days_per_book integer NOT NULL,
    renewal_attempt_count integer,
    cancelled_at timestamp(6) without time zone,
    end_date timestamp(6) without time zone NOT NULL,
    last_renewal_attempt timestamp(6) without time zone,
    next_billing_date timestamp(6) without time zone,
    price bigint NOT NULL,
    start_date timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    subscription_plan_id uuid NOT NULL,
    plan_code character varying(50) NOT NULL,
    plan_name character varying(100) NOT NULL,
    cancellation_reason character varying(255)
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    create_at timestamp(6) without time zone,
    last_login timestamp(6) without time zone,
    update_at timestamp(6) without time zone,
    id uuid NOT NULL,
    auth_provider character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    full_name character varying(255) NOT NULL,
    google_id character varying(255),
    password character varying(255),
    phone character varying(255),
    profile_image character varying(255),
    role character varying(255) NOT NULL,
    CONSTRAINT users_auth_provider_check CHECK (((auth_provider)::text = ANY ((ARRAY['LOCAL'::character varying, 'GOOGLE'::character varying])::text[]))),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ROLE_USER'::character varying, 'ROLE_ADMIN'::character varying])::text[])))
);


--
-- Name: wishlist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wishlist (
    added_at timestamp(6) without time zone,
    book_id uuid NOT NULL,
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    notes character varying(500)
);


--
-- Name: book book_isbn_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.book
    ADD CONSTRAINT book_isbn_key UNIQUE (isbn);


--
-- Name: book_loan book_loan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.book_loan
    ADD CONSTRAINT book_loan_pkey PRIMARY KEY (id);


--
-- Name: book book_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.book
    ADD CONSTRAINT book_pkey PRIMARY KEY (id);


--
-- Name: book_review book_review_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.book_review
    ADD CONSTRAINT book_review_pkey PRIMARY KEY (id);


--
-- Name: fine fine_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fine
    ADD CONSTRAINT fine_pkey PRIMARY KEY (id);


--
-- Name: genre genre_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.genre
    ADD CONSTRAINT genre_code_key UNIQUE (code);


--
-- Name: genre genre_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.genre
    ADD CONSTRAINT genre_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_token_key UNIQUE (token);


--
-- Name: payable payable_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payable
    ADD CONSTRAINT payable_pkey PRIMARY KEY (id);


--
-- Name: payment payment_charge_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_charge_id_key UNIQUE (charge_id);


--
-- Name: payment payment_checkout_session_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_checkout_session_id_key UNIQUE (checkout_session_id);


--
-- Name: payment payment_payment_intent_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_payment_intent_id_key UNIQUE (payment_intent_id);


--
-- Name: payment payment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (id);


--
-- Name: payment payment_refund_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_refund_id_key UNIQUE (refund_id);


--
-- Name: payment payment_transaction_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_transaction_id_key UNIQUE (transaction_id);


--
-- Name: refresh_token refresh_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT refresh_token_pkey PRIMARY KEY (id);


--
-- Name: refresh_token refresh_token_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT refresh_token_token_key UNIQUE (token);


--
-- Name: reservation reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_pkey PRIMARY KEY (id);


--
-- Name: stripe_webhook_events stripe_webhook_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stripe_webhook_events
    ADD CONSTRAINT stripe_webhook_events_pkey PRIMARY KEY (event_id);


--
-- Name: subscription_plan subscription_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscription_plan
    ADD CONSTRAINT subscription_plan_pkey PRIMARY KEY (id);


--
-- Name: subscription_plan subscription_plan_plan_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscription_plan
    ADD CONSTRAINT subscription_plan_plan_code_key UNIQUE (plan_code);


--
-- Name: subscriptions subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT subscriptions_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: wishlist wishlist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlist
    ADD CONSTRAINT wishlist_pkey PRIMARY KEY (id);


--
-- Name: wishlist wishlist_user_id_book_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlist
    ADD CONSTRAINT wishlist_user_id_book_id_key UNIQUE (user_id, book_id);


--
-- Name: idx_book_review_book_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_book_review_book_created ON public.book_review USING btree (book_id, created_at);


--
-- Name: idx_book_review_book_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_book_review_book_id ON public.book_review USING btree (book_id);


--
-- Name: idx_book_review_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_book_review_user_id ON public.book_review USING btree (user_id);


--
-- Name: idx_fine_book_loan; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fine_book_loan ON public.fine USING btree (book_loan_id);


--
-- Name: idx_fine_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fine_status ON public.fine USING btree (status);


--
-- Name: idx_loan_return_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_loan_return_date ON public.book_loan USING btree (return_date);


--
-- Name: idx_loan_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_loan_status ON public.book_loan USING btree (status);


--
-- Name: idx_loan_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_loan_user ON public.book_loan USING btree (user_id);


--
-- Name: idx_payable_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payable_created ON public.payable USING btree (created_at);


--
-- Name: idx_payable_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payable_user ON public.payable USING btree (user_id);


--
-- Name: idx_payment_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_created ON public.payment USING btree (created_at);


--
-- Name: idx_payment_intent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_intent ON public.payment USING btree (payment_intent_id);


--
-- Name: idx_payment_session; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_session ON public.payment USING btree (checkout_session_id);


--
-- Name: idx_payment_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_status ON public.payment USING btree (payment_status);


--
-- Name: idx_payment_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_user ON public.payment USING btree (user_id);


--
-- Name: idx_refresh_token_token; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refresh_token_token ON public.refresh_token USING btree (token);


--
-- Name: idx_reservation_book; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_book ON public.reservation USING btree (book_id);


--
-- Name: idx_reservation_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_status ON public.reservation USING btree (status);


--
-- Name: idx_reservation_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_user ON public.reservation USING btree (user_id);


--
-- Name: idx_subscription_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subscription_active ON public.subscriptions USING btree (active);


--
-- Name: idx_subscription_end_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subscription_end_date ON public.subscriptions USING btree (end_date);


--
-- Name: idx_wishlist_book; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_wishlist_book ON public.wishlist USING btree (book_id);


--
-- Name: idx_wishlist_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_wishlist_user ON public.wishlist USING btree (user_id);


--
-- Name: book_review fk29oatdl4f30mtg65oxo1nkmjg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.book_review
    ADD CONSTRAINT fk29oatdl4f30mtg65oxo1nkmjg FOREIGN KEY (book_id) REFERENCES public.book(id);


--
-- Name: fine fk3xs1uii642wqk59ck5e86kfrr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fine
    ADD CONSTRAINT fk3xs1uii642wqk59ck5e86kfrr FOREIGN KEY (waived_by_id) REFERENCES public.users(id);


--
-- Name: book_loan fk7udisqc789s350w3g5rcl4qdc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.book_loan
    ADD CONSTRAINT fk7udisqc789s350w3g5rcl4qdc FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: wishlist fk94k0l1f4gpde7nw2scncp8pp4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlist
    ADD CONSTRAINT fk94k0l1f4gpde7nw2scncp8pp4 FOREIGN KEY (book_id) REFERENCES public.book(id);


--
-- Name: fine fk9nm4m3dxla7v33cc6842wp53b; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fine
    ADD CONSTRAINT fk9nm4m3dxla7v33cc6842wp53b FOREIGN KEY (id) REFERENCES public.payable(id);


--
-- Name: subscriptions fkcc9p7udtvhienegrsa45rsjmj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT fkcc9p7udtvhienegrsa45rsjmj FOREIGN KEY (id) REFERENCES public.payable(id);


--
-- Name: genre fkcvbnwpnfnuwxhkvqcxvvtrumk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.genre
    ADD CONSTRAINT fkcvbnwpnfnuwxhkvqcxvvtrumk FOREIGN KEY (parent_genre_id) REFERENCES public.genre(id);


--
-- Name: book_loan fkd0pola0sj2fp9719x3b8lpb7s; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.book_loan
    ADD CONSTRAINT fkd0pola0sj2fp9719x3b8lpb7s FOREIGN KEY (book_id) REFERENCES public.book(id);


--
-- Name: fine fkgpai641cxnsfm98ewwyvyqunn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fine
    ADD CONSTRAINT fkgpai641cxnsfm98ewwyvyqunn FOREIGN KEY (book_loan_id) REFERENCES public.book_loan(id);


--
-- Name: reservation fkirxtcw4s6lhwi6l9ocrk6bjfy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT fkirxtcw4s6lhwi6l9ocrk6bjfy FOREIGN KEY (book_id) REFERENCES public.book(id);


--
-- Name: subscriptions fkiu8wnm0oh1p9ppfddpg4kcr2u; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT fkiu8wnm0oh1p9ppfddpg4kcr2u FOREIGN KEY (subscription_plan_id) REFERENCES public.subscription_plan(id);


--
-- Name: refresh_token fkjtx87i0jvq2svedphegvdwcuy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT fkjtx87i0jvq2svedphegvdwcuy FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: password_reset_tokens fkk3ndxg5xp6v7wd4gjyusp15gq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT fkk3ndxg5xp6v7wd4gjyusp15gq FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: payment fkkt4gb85q2od3kvqvkwq5pdgst; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT fkkt4gb85q2od3kvqvkwq5pdgst FOREIGN KEY (payable_id) REFERENCES public.payable(id);


--
-- Name: book fkm1t3yvw5i7olwdf32cwuul7ta; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.book
    ADD CONSTRAINT fkm1t3yvw5i7olwdf32cwuul7ta FOREIGN KEY (genre_id) REFERENCES public.genre(id);


--
-- Name: payment fkmi2669nkjesvp7cd257fptl6f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT fkmi2669nkjesvp7cd257fptl6f FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: book_review fkntncp0b191bex8jkm3vy3l13x; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.book_review
    ADD CONSTRAINT fkntncp0b191bex8jkm3vy3l13x FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: payable fkqfyxpnrni6ct1h6akcndf30t5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payable
    ADD CONSTRAINT fkqfyxpnrni6ct1h6akcndf30t5 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: reservation fkrea93581tgkq61mdl13hehami; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT fkrea93581tgkq61mdl13hehami FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: fine fkrk0o290eyqs5ojotw6pbqv1eu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fine
    ADD CONSTRAINT fkrk0o290eyqs5ojotw6pbqv1eu FOREIGN KEY (processed_by_user_id) REFERENCES public.users(id);


--
-- Name: wishlist fktrd6335blsefl2gxpb8lr0gr7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wishlist
    ADD CONSTRAINT fktrd6335blsefl2gxpb8lr0gr7 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--


