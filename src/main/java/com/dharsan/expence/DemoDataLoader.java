package com.dharsan.expence;

import com.dharsan.expence.entity.Employee;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataLoader {

    @Bean
    CommandLineRunner loadEmployees(EmployeeRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Employee(null, "Ananya Rao", "ananya@company.example", "EMPLOYEE"));
                repository.save(new Employee(null, "Vikram Shah", "vikram@company.example", "MANAGER"));
            }
        };
    }
}