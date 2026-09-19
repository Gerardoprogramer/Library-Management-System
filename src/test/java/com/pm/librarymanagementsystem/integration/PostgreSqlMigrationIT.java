package com.pm.librarymanagementsystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class PostgreSqlMigrationIT {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:16-alpine"
            )
                    .withDatabaseName("library_test")
                    .withUsername("library_test")
                    .withPassword("library_test");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry
    ) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayShouldApplyAllMigrations() {

        List<String> versions =
                jdbcTemplate.queryForList(
                        """
                        SELECT version
                        FROM flyway_schema_history
                        WHERE success = true
                          AND version IS NOT NULL
                        ORDER BY installed_rank
                        """,
                        String.class
                );

        assertEquals(
                List.of("1", "2", "3"),
                versions
        );
    }

    @Test
    void databaseShouldRejectBookWithAvailableCopiesGreaterThanTotalCopies() {

        UUID genreId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        jdbcTemplate.update(
                """
                INSERT INTO genre (
                    id,
                    active,
                    display_order,
                    code,
                    name
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                genreId,
                true,
                1,
                "TEST-" + genreId,
                "Test Genre"
        );

        DataIntegrityViolationException exception =
                assertThrows(
                        DataIntegrityViolationException.class,
                        () -> jdbcTemplate.update(
                                """
                                INSERT INTO book (
                                    id,
                                    active,
                                    available_copies,
                                    total_copies,
                                    created_at,
                                    updated_at,
                                    genre_id,
                                    isbn,
                                    author,
                                    title
                                )
                                VALUES (?, ?, ?, ?, NOW(), NOW(), ?, ?, ?, ?)
                                """,
                                bookId,
                                true,
                                5,
                                2,
                                genreId,
                                "ISBN-" + bookId,
                                "Test Author",
                                "Invalid Book"
                        )
                );

        assertNotNull(exception);
    }

    @Test
    void databaseShouldRejectNonNormalizedUserEmail() {

        UUID userId = UUID.randomUUID();

        DataIntegrityViolationException exception =
                assertThrows(
                        DataIntegrityViolationException.class,
                        () -> jdbcTemplate.update(
                                """
                                INSERT INTO users (
                                    id,
                                    auth_provider,
                                    email,
                                    full_name,
                                    role
                                )
                                VALUES (?, ?, ?, ?, ?)
                                """,
                                userId,
                                "LOCAL",
                                "  GERARDO@TEST.COM  ",
                                "Gerardo Test",
                                "ROLE_USER"
                        )
                );

        assertNotNull(exception);
    }

    @Test
    void databaseShouldRejectPaymentWithNegativeAmount() {

        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        jdbcTemplate.update(
                """
                INSERT INTO users (
                    id,
                    auth_provider,
                    email,
                    full_name,
                    role
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                userId,
                "LOCAL",
                "payment-" + userId + "@test.com",
                "Payment Test User",
                "ROLE_USER"
        );

        DataIntegrityViolationException exception =
                assertThrows(
                        DataIntegrityViolationException.class,
                        () -> jdbcTemplate.update(
                                """
                                INSERT INTO payment (
                                    id,
                                    user_id,
                                    amount,
                                    renewal_payment,
                                    currency,
                                    payment_status,
                                    payment_type,
                                    payment_gateway
                                )
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                                """,
                                paymentId,
                                userId,
                                -10.00,
                                false,
                                "USD",
                                "PENDING",
                                "FINE",
                                "STRIPE"
                        )
                );

        assertNotNull(exception);
    }

    @Test
    void databaseShouldRejectTwoActiveLoansForSameUserAndBook() {

        UUID userId = insertTestUser();
        UUID bookId = insertTestBook();

        UUID firstLoanId = UUID.randomUUID();
        UUID secondLoanId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(
                """
                INSERT INTO book_loan (
                    id,
                    book_id,
                    user_id,
                    type,
                    status,
                    checkout_date,
                    due_date,
                    renewal_count,
                    max_renewals,
                    overdue,
                    overdue_days,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """,
                firstLoanId,
                bookId,
                userId,
                "CHECKOUT",
                "CHECKED_OUT",
                now,
                now.plusDays(7),
                0,
                2,
                false,
                0
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO book_loan (
                            id,
                            book_id,
                            user_id,
                            type,
                            status,
                            checkout_date,
                            due_date,
                            renewal_count,
                            max_renewals,
                            overdue,
                            overdue_days,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                        """,
                        secondLoanId,
                        bookId,
                        userId,
                        "CHECKOUT",
                        "OVERDUE",
                        now,
                        now.plusDays(7),
                        0,
                        2,
                        true,
                        1
                )
        );
    }

    @Test
    void databaseShouldAllowNewActiveLoanAfterPreviousLoanWasReturned() {

        UUID userId = insertTestUser();
        UUID bookId = insertTestBook();

        LocalDateTime now = LocalDateTime.now();

        int firstInsert =
                jdbcTemplate.update(
                        """
                        INSERT INTO book_loan (
                            id,
                            book_id,
                            user_id,
                            type,
                            status,
                            checkout_date,
                            due_date,
                            return_date,
                            renewal_count,
                            max_renewals,
                            overdue,
                            overdue_days,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                        """,
                        UUID.randomUUID(),
                        bookId,
                        userId,
                        "CHECKOUT",
                        "RETURNED",
                        now.minusDays(10),
                        now.minusDays(3),
                        now.minusDays(2),
                        0,
                        2,
                        false,
                        0
                );

        int secondInsert =
                jdbcTemplate.update(
                        """
                        INSERT INTO book_loan (
                            id,
                            book_id,
                            user_id,
                            type,
                            status,
                            checkout_date,
                            due_date,
                            renewal_count,
                            max_renewals,
                            overdue,
                            overdue_days,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                        """,
                        UUID.randomUUID(),
                        bookId,
                        userId,
                        "CHECKOUT",
                        "CHECKED_OUT",
                        now,
                        now.plusDays(7),
                        0,
                        2,
                        false,
                        0
                );

        assertEquals(1, firstInsert);
        assertEquals(1, secondInsert);
    }

    @Test
    void databaseShouldRejectTwoActiveReservationsForSameUserAndBook() {

        UUID userId = insertTestUser();
        UUID bookId = insertTestBook();

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(
                """
                INSERT INTO reservation (
                    id,
                    book_id,
                    user_id,
                    status,
                    notification_sent,
                    reserved_at,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
                """,
                UUID.randomUUID(),
                bookId,
                userId,
                "PENDING",
                false,
                now
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO reservation (
                            id,
                            book_id,
                            user_id,
                            status,
                            notification_sent,
                            reserved_at,
                            available_at,
                            available_until,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                        """,
                        UUID.randomUUID(),
                        bookId,
                        userId,
                        "AVAILABLE",
                        false,
                        now.plusMinutes(1),
                        now.plusMinutes(1),
                        now.plusHours(48)
                )
        );
    }

    @Test
    void databaseShouldAllowNewReservationAfterPreviousReservationWasCancelled() {

        UUID userId = insertTestUser();
        UUID bookId = insertTestBook();

        LocalDateTime now = LocalDateTime.now();

        int cancelledInsert =
                jdbcTemplate.update(
                        """
                        INSERT INTO reservation (
                            id,
                            book_id,
                            user_id,
                            status,
                            notification_sent,
                            reserved_at,
                            cancelled_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                        """,
                        UUID.randomUUID(),
                        bookId,
                        userId,
                        "CANCELLED",
                        false,
                        now.minusDays(2),
                        now.minusDays(1)
                );

        int pendingInsert =
                jdbcTemplate.update(
                        """
                        INSERT INTO reservation (
                            id,
                            book_id,
                            user_id,
                            status,
                            notification_sent,
                            reserved_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
                        """,
                        UUID.randomUUID(),
                        bookId,
                        userId,
                        "PENDING",
                        false,
                        now
                );

        assertEquals(1, cancelledInsert);
        assertEquals(1, pendingInsert);
    }

    private UUID insertTestUser() {

        UUID userId = UUID.randomUUID();

        jdbcTemplate.update(
                """
                INSERT INTO users (
                    id,
                    auth_provider,
                    email,
                    full_name,
                    role
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                userId,
                "LOCAL",
                "user-" + userId + "@test.com",
                "Integration Test User",
                "ROLE_USER"
        );

        return userId;
    }

    private UUID insertTestBook() {

        UUID genreId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        jdbcTemplate.update(
                """
                INSERT INTO genre (
                    id,
                    active,
                    display_order,
                    code,
                    name
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                genreId,
                true,
                1,
                "GENRE-" + genreId,
                "Integration Genre"
        );

        jdbcTemplate.update(
                """
                INSERT INTO book (
                    id,
                    active,
                    available_copies,
                    total_copies,
                    created_at,
                    updated_at,
                    genre_id,
                    isbn,
                    author,
                    title
                )
                VALUES (?, ?, ?, ?, NOW(), NOW(), ?, ?, ?, ?)
                """,
                bookId,
                true,
                3,
                3,
                genreId,
                "ISBN-" + bookId,
                "Integration Author",
                "Integration Book"
        );

        return bookId;
    }
}