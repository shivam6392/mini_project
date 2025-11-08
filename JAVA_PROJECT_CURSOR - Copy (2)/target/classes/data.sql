-- Insert departments
INSERT INTO departments (name) VALUES ('HR');
INSERT INTO departments (name) VALUES ('IT');
INSERT INTO departments (name) VALUES ('Finance');

-- Insert roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_HR');
INSERT INTO roles (name) VALUES ('ROLE_MANAGER');
INSERT INTO roles (name) VALUES ('ROLE_EMPLOYEE');

-- Insert admin user (password is 'admin123')
-- Stored with {noop} prefix so the delegating password encoder accepts the plain password in dev
INSERT INTO users (username, password, full_name, email, department_id) 
VALUES ('admin', '{noop}admin123', 'System Admin', 'admin@example.com', 
    (SELECT id FROM departments WHERE name = 'IT'));