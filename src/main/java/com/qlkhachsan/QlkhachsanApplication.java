package com.qlkhachsan;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QlkhachsanApplication {

    public static void main(String[] args) {
        SpringApplication.run(QlkhachsanApplication.class, args);
    }

    @Bean
    CommandLineRunner testCassandra(CqlSession session) {

        return args -> {

            System.out.println();
            System.out.println("================================");
            System.out.println("   CASSANDRA CONNECTION TEST");
            System.out.println("================================");

            try {
                var result = session.execute(
                        "SELECT * FROM hotels"
                );

                result.forEach(row -> {

                    System.out.println(
                            row.getString("hotel_id")
                                    + " | "
                                    + row.getString("hotel_name")
                                    + " | "
                                    + row.getString("city")
                    );
                });

                System.out.println("================================");
                System.out.println("   CONNECTION SUCCESS!");
                System.out.println("================================");
            } catch (Exception e) {
                // Không để lỗi query/DB làm sập cả web UI.
                System.out.println("================================");
                System.out.println("   CASSANDRA QUERY FAILED (app vẫn chạy): " + e.getMessage());
                System.out.println("================================");
            }
        };
    }
}