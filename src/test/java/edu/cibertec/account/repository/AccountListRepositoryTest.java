package edu.cibertec.account.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.junit.jupiter.api.AfterEach;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class AccountListRepositoryTest {

    private EmbeddedDatabase database;
    private AccountListRepository repository;

    @BeforeEach
    void setUp() {
        database = new EmbeddedDatabaseBuilder().generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2).build();
        var jdbc = new JdbcTemplate(database);
        jdbc.execute("CREATE SCHEMA account");
        jdbc.execute("CREATE SCHEMA payment");
        jdbc.execute("CREATE SCHEMA products");
        jdbc.execute("CREATE TABLE account.status (idstatus BIGINT PRIMARY KEY, name VARCHAR(20))");
        jdbc.execute("CREATE TABLE account.roles (idrole BIGINT PRIMARY KEY, name VARCHAR(20))");
        jdbc.update("INSERT INTO account.roles VALUES (1, 'ADMIN'), (2, 'USER'), (3, 'SELLER')");
        jdbc.execute("""
                CREATE TABLE account.users (
                    iduser BIGINT PRIMARY KEY, name VARCHAR(80), lastname VARCHAR(80),
                    username VARCHAR(80), idrole BIGINT, email VARCHAR(120),
                    nro VARCHAR(20), direccion VARCHAR(180), idstatus BIGINT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
                """);
        jdbc.execute("CREATE TABLE payment.state_sold (id_state_sold BIGINT PRIMARY KEY, name_state VARCHAR(50))");
        jdbc.execute("CREATE TABLE products.product (idproduct BIGINT PRIMARY KEY, sale_price NUMERIC(12,2))");
        jdbc.execute("""
                CREATE TABLE payment.products_sold (
                    id_products_sold BIGINT PRIMARY KEY, id_products BIGINT,
                    id_user BIGINT, number_sold INTEGER, id_state_sold BIGINT)
                """);
        jdbc.update("INSERT INTO account.status VALUES (1, 'active'), (2, 'inactive')");
        jdbc.update("""
                INSERT INTO account.users (iduser, name, lastname, username, idrole, email, nro, direccion, idstatus) VALUES
                    (1, 'Ana', 'Torres', 'ana', 2, 'ana@demo.com', '999888777', 'Av. Lima 123', 1),
                    (2, 'Luis', 'Perez', 'luis', 2, 'luis@demo.com', '999888778', 'Av. Lima 456', 2),
                    (3, 'Maria', 'Lopez', 'maria', 2, 'maria@demo.com', '999888779', 'Av. Lima 789', 1)
                """);
        jdbc.update("INSERT INTO payment.state_sold VALUES (1, 'PAGADO'), (2, 'PENDIENTE'), (3, 'CANCELADO')");
        jdbc.update("INSERT INTO products.product VALUES (1, 10.25), (2, 5.10)");
        jdbc.update("""
                INSERT INTO payment.products_sold VALUES
                    (1, 1, 1, 2, 1), (2, 2, 1, 3, 1),
                    (3, 1, 1, 100, 2), (4, 1, 1, 100, 3),
                    (5, 1, 2, 100, 2), (6, 1, 2, 100, 3)
                """);
        repository = new AccountListRepository(database);
    }

    @AfterEach
    void tearDown() {
        database.shutdown();
    }

    @Test
    void countsPaidOrdersAndSumsQuantitiesAtSalePriceWithoutPendingOrCancelledOrders() {
        var accounts = repository.findAll(PageRequest.of(0, 20), null, 0).getContent();

        assertThat(accounts).hasSize(3);
        var ana = accounts.getFirst();
        assertThat(ana.idUser()).isEqualTo(1L);
        assertThat(ana.name()).isEqualTo("Ana");
        assertThat(ana.lastname()).isEqualTo("Torres");
        assertThat(ana.email()).isEqualTo("ana@demo.com");
        assertThat(ana.nro()).isEqualTo("999888777");
        assertThat(ana.direccion()).isEqualTo("Av. Lima 123");
        assertThat(ana.status()).isEqualTo("active");
        assertThat(ana.role()).isEqualTo("USER");
        assertThat(ana.createdAt()).isNotNull();
        assertThat(ana.paidOrderCount()).isEqualTo(2);
        assertThat(ana.totalPurchases()).isEqualByComparingTo("35.80");
    }

    @Test
    void includesInactiveUsersAndUsersWithoutPaidOrdersWithZeroTotals() {
        var accounts = repository.findAll(PageRequest.of(0, 20), null, 0).getContent();

        assertThat(accounts.get(1).status()).isEqualTo("inactive");
        assertThat(accounts.subList(1, 3)).allSatisfy(account -> {
            assertThat(account.paidOrderCount()).isZero();
            assertThat(account.totalPurchases()).isEqualByComparingTo("0");
        });
    }

    @Test
    void paginatesWithStableOrderAndFilteredTotalsIncludingPagesBeyondTheEnd() {
        var first = repository.findAll(PageRequest.of(0, 1), true, 0);
        var second = repository.findAll(PageRequest.of(1, 1), true, 0);
        var beyond = repository.findAll(PageRequest.of(2, 1), true, 0);

        assertThat(first.getContent()).extracting(account -> account.idUser()).containsExactly(1L);
        assertThat(second.getContent()).extracting(account -> account.idUser()).containsExactly(3L);
        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(first.isFirst()).isTrue();
        assertThat(second.isLast()).isTrue();
        assertThat(beyond.getContent()).isEmpty();
        assertThat(beyond.getTotalElements()).isEqualTo(2);
    }

    @Test
    void filtersInactiveUsers() {
        var result = repository.findAll(PageRequest.of(0, 20), false, 0);

        assertThat(result.getContent()).extracting(account -> account.idUser()).containsExactly(2L);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void staffIncludesOnlyAdminAndSellerBeforePaginationAndCombinesFilters() {
        var jdbc = new JdbcTemplate(database);
        jdbc.update("UPDATE account.roles SET idrole = 5 WHERE name = 'SELLER'");
        jdbc.update("UPDATE account.users SET idrole = 1 WHERE iduser = 1");
        jdbc.update("UPDATE account.users SET idrole = 5 WHERE iduser = 2");

        var first = repository.findStaff(PageRequest.of(0, 1), null, 0);
        var second = repository.findStaff(PageRequest.of(1, 1), null, 0);
        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(first.getContent()).extracting(account -> account.role()).containsExactly("ADMIN");
        assertThat(second.getContent()).extracting(account -> account.role()).containsExactly("SELLER");
        assertThat(repository.findStaff(PageRequest.of(0, 20), true, 0).getContent())
                .extracting(account -> account.idUser()).containsExactly(1L);
        assertThat(repository.findStaff(PageRequest.of(0, 20), false, 0).getContent())
                .extracting(account -> account.idUser()).containsExactly(2L);
        assertThat(repository.findStaff(PageRequest.of(0, 20), null, 2).getContent())
                .extracting(account -> account.idUser()).containsExactly(1L);
        assertThat(repository.findStaff(PageRequest.of(0, 20), null, 5).getTotalElements()).isZero();
        assertThat(repository.findAll(PageRequest.of(0, 20), null, 0).getTotalElements()).isEqualTo(3);
    }

    @Test
    void moreThanFourPurchasesRequiresFivePaidOrdersAndCombinesWithStatus() {
        var jdbc = new JdbcTemplate(database);
        jdbc.update("""
                INSERT INTO payment.products_sold VALUES
                    (7, 1, 1, 1, 1), (8, 1, 1, 1, 1),
                    (9, 1, 2, 1, 1), (10, 1, 2, 1, 1), (11, 1, 2, 1, 1),
                    (12, 1, 2, 1, 1), (13, 1, 2, 1, 1)
                """);
        // Ana has four paid orders plus pending/cancelled orders; Luis has five paid orders.
        var all = repository.findAll(PageRequest.of(0, 20), null, 5);
        var active = repository.findAll(PageRequest.of(0, 20), true, 5);
        var inactive = repository.findAll(PageRequest.of(0, 20), false, 5);

        assertThat(all.getContent()).extracting(account -> account.idUser()).containsExactly(2L);
        assertThat(all.getContent().getFirst().paidOrderCount()).isEqualTo(5);
        assertThat(all.getTotalElements()).isEqualTo(1);
        assertThat(active.getContent()).isEmpty();
        assertThat(active.getTotalElements()).isZero();
        assertThat(inactive.getTotalElements()).isEqualTo(1);
    }
}
