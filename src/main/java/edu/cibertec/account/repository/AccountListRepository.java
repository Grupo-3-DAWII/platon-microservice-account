package edu.cibertec.account.repository;

import edu.cibertec.account.dto.AccountListResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Repository
public class AccountListRepository {

    private final JdbcClient jdbcClient;

    public AccountListRepository(DataSource dataSource) {
        this.jdbcClient = JdbcClient.create(dataSource);
    }

    public Page<AccountListResponse> findAll(Pageable pageable, Boolean active, int minPaidOrders) {
        return findAccounts(pageable, active, minPaidOrders, false);
    }

    public Page<AccountListResponse> findStaff(Pageable pageable, Boolean active, int minPaidOrders) {
        return findAccounts(pageable, active, minPaidOrders, true);
    }

    private Page<AccountListResponse> findAccounts(Pageable pageable, Boolean active, int minPaidOrders,
                                                  boolean staffOnly) {
        String columns = """
                SELECT buyer.iduser, buyer.name, buyer.lastname, buyer.username,
                       buyer.idrole, user_role.name AS role, buyer.email, buyer.nro, buyer.direccion,
                       buyer.idstatus, user_status.name AS status, buyer.created_at,
                       COALESCE(purchases.paid_order_count, 0) AS paid_order_count,
                       COALESCE(purchases.total_purchases, 0) AS total_purchases
                """;
        String from = """
                FROM account.users buyer
                JOIN account.status user_status ON user_status.idstatus = buyer.idstatus
                JOIN account.roles user_role ON user_role.idrole = buyer.idrole
                LEFT JOIN (
                    SELECT sale.id_user, COUNT(*) AS paid_order_count,
                           SUM(sale.number_sold * product.sale_price) AS total_purchases
                    FROM payment.products_sold sale
                    JOIN payment.state_sold sale_status
                        ON sale_status.id_state_sold = sale.id_state_sold
                    JOIN products.product product ON product.idproduct = sale.id_products
                    WHERE UPPER(TRIM(sale_status.name_state)) = 'PAGADO'
                    GROUP BY sale.id_user
                ) purchases ON purchases.id_user = buyer.iduser
                WHERE COALESCE(purchases.paid_order_count, 0) >= :minPaidOrders
                """;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("minPaidOrders", minPaidOrders);
        if (staffOnly) {
            from += " AND user_role.name IN (:roles)";
            parameters.put("roles", java.util.List.of("ADMIN", "SELLER"));
        }
        if (active != null) {
            from += " AND LOWER(TRIM(user_status.name)) = :status";
            parameters.put("status", active ? "active" : "inactive");
        }
        long total = jdbcClient.sql("SELECT COUNT(*) " + from)
                .params(parameters).query(Long.class).single();
        var content = jdbcClient.sql(columns + from + " ORDER BY buyer.iduser LIMIT :limit OFFSET :offset")
                .params(parameters)
                .param("limit", pageable.getPageSize())
                .param("offset", pageable.getOffset())
                .query((rs, rowNum) -> new AccountListResponse(
                        rs.getLong("iduser"),
                        rs.getString("name"),
                        rs.getString("lastname"),
                        rs.getString("username"),
                        rs.getLong("idrole"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("nro"),
                        rs.getString("direccion"),
                        rs.getLong("idstatus"),
                        rs.getString("status"),
                        rs.getLong("paid_order_count"),
                        rs.getBigDecimal("total_purchases"),
                        rs.getObject("created_at", java.time.LocalDateTime.class)))
                .list();
        return new PageImpl<>(content, pageable, total);
    }
}
