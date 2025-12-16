INSERT INTO service(id, created, deletion_date, first_name, last_name, login, service_name, state, structure_id, type, user_id)
VALUES ('11430e8b-090b-4562-8354-4522d37fdde2', now(), '2024-09-20 14:55:36.088093', 'John', 'DOE', 'john.doe', 'wp-doe', 'SCHEDULED',
        '3d3bba2b-7bba-4e0a-94af-c5d1b3332f77', 'WORDPRESS', '6acd2855-3663-4d95-a888-da95974feb74');
INSERT INTO service(id, created, deletion_date, first_name, last_name, login, service_name, state, structure_id, type, user_id, admin_user, admin_password)
VALUES ('a46bddf4-788c-4aa2-9b1f-655cfd287838', now(), '2024-09-20 14:55:36.088093', 'John', 'DOE', 'john.doe', 'presta-doe', 'SCHEDULED',
        '3d3bba2b-7bba-4e0a-94af-c5d1b3332f77', 'PRESTASHOP', '6acd2855-3663-4d95-a888-da95974feb742', 'root@yopmail.com', 'password');

-- INSERT INTO service(id, created, deletion_date, first_name, last_name, service_name, state, structure_id, type, user_id)
-- VALUES ('2d6aaf50-0799-4bc2-983a-4b1393b1a1c4', now(), now(), 'Jane', 'DOE', 'presta-jane', 'DELETION_SCHEDULED',
--         '3d3bba2b-7bba-4e0a-94af-c5d1b3332f77', 'WORDPRESS', '23e65546-42ca-4a6c-af0b-5dc389da4975');



INSERT INTO database(id, host, port, username, password, default_db, ssl, ca_cert)
VALUES ('aa6ceae7-3485-4c54-98dd-2e5ddde4fe61', 'mysql-73dc4a5d-o998d46c3.database.cloud.ovh.net', 20184, 'agent',
        '5e67fz3AxDYtGPTLSpa8', 'defaultdb', true,
        '-----BEGIN CERTIFICATE-----MIIEQTCCAqmgAwIBAgIUTQ+0HX+e8a0fuvD0T7wY03iZLJgwDQYJKoZIhvcNAQEMBQAwOjE4MDYGA1UEAwwvMTIxZjliMTItYjFiNS00YzAwLWExYzgtZDU0Yzg1ZjgyYTQ4IFByb2plY3QgQ0EwHhcNMjQwOTE2MTQzNTE0WhcNMzQwOTE0MTQzNTE0WjA6MTgwNgYDVQQDDC8xMjFmOWIxMi1iMWI1LTRjMDAtYTFjOC1kNTRjODVmODJhNDggUHJvamVjdCBDQTCCAaIwDQYJKoZIhvcNAQEBBQADggGPADCCAYoCggGBAOb448bbxcFcBqRmuaq/xgGlLF0XUzZlDN7WWd5Mzdw9LX3pZTth3rk/+QXl9dF5C5NjWbw3uaki1uen84FANyzzP6PoCsY4dKamfg4Rd2R753Z/niZSCk36Z9OZ7ndjYF2RuUDdqz0AFbleQLl09XRESV9xlekNU8UKgkmpcJ+W9xYtk/ALOf0XpQDHv1uytxDZZiqjvEEp1IqPhFvvUE++WB/Z9VPmpdv6IlE2n9KulCjYUWkxjBKggqVVNgyrRcYF8/ObNQXaHgX7AOj0aIPY/GXYX873Qjq73Bpc4qGkiYpLfEhslnA2OsemnZjCH83YM88DM83BJLu8KMGklkBS0ndKa2HdKIyTDCwzC6spe5I0mdLRs2MCPPKtUM0vlKajbEAusjjuay0MosN4ZFbf55/H8X83ojyODazturGdGwpiUf4Njt0vWGoX1Y4BDnSv4azkHk7YJSVtraFo/18EbsinOdsh9Z6BS4eyQzRs2p9TXJw9Ta0ECs2GGm4YOQIDAQABoz8wPTAdBgNVHQ4EFgQUCatvZ3g2zyGrvp7m6OFy3B/Z5lcwDwYDVR0TBAgwBgEB/wIBADALBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQEMBQADggGBAJBomC72fdKsOnDRwHXKK2FfbdjknCqy/Sw/TCzvcFmOXS8g4m/D7e3j3z5FKkdDwPKZtZkgWmGn4HU0rJSkxi1D9YAQ3oGsGALOFnhWJxrh9I6bMvOYE4HyaFrQM3XOIGCQ4SEkNqaveGtnZnU7nJCNXmARryIjIvdkicgURxD3XrXswjmkyhjsTItozkK8dVxD73rSYjkX4GFh1ABxL7ABj09w7rcmCzsjqGfVlfHWVXLM3dnQMNgGjFs8GY5TZc85M0FGxX3QQPFP0sxgwZahRhgaio53CfsdIwS5XlIm1T6H0SSkfGy7vzinQLM7oMsHjBQLtl3axe8oA5JznozfGKKOCoNb71lsLCLuzaI1ayLDyekb6L4GqCQNg0QbPx/5NlyzraA1YYgHhiJMXUqcudJHPoVQuKyHeRQ7RcQewZucKuk/IxGBctSF+kUPvlCIyIn9hR4dtQ4XUV9SLczln48fthdwsv3UX0g/i1eyQFwytIcbPFipSxZ09/i2Gg==-----END CERTIFICATE-----');

-- test dev
-- INSERT INTO database(id, host, port, username, password, default_db, ssl, ca_cert)
-- VALUES ('fkjcsed7-3485-4c54-97nn-455ddde4f619', 'localhost', 3306, 'root',
--         'password', 'mysql', false,
--         '-----BEGIN CERTIFICATE-----MIIEQTCCAqmgAwIBAgIUTQ+0HX+e8a0fuvD0T7wY03iZLJgwDQYJKoZIhvcNAQEMBQAwOjE4MDYGA1UEAwwvMTIxZjliMTItYjFiNS00YzAwLWExYzgtZDU0Yzg1ZjgyYTQ4IFByb2plY3QgQ0EwHhcNMjQwOTE2MTQzNTE0WhcNMzQwOTE0MTQzNTE0WjA6MTgwNgYDVQQDDC8xMjFmOWIxMi1iMWI1LTRjMDAtYTFjOC1kNTRjODVmODJhNDggUHJvamVjdCBDQTCCAaIwDQYJKoZIhvcNAQEBBQADggGPADCCAYoCggGBAOb448bbxcFcBqRmuaq/xgGlLF0XUzZlDN7WWd5Mzdw9LX3pZTth3rk/+QXl9dF5C5NjWbw3uaki1uen84FANyzzP6PoCsY4dKamfg4Rd2R753Z/niZSCk36Z9OZ7ndjYF2RuUDdqz0AFbleQLl09XRESV9xlekNU8UKgkmpcJ+W9xYtk/ALOf0XpQDHv1uytxDZZiqjvEEp1IqPhFvvUE++WB/Z9VPmpdv6IlE2n9KulCjYUWkxjBKggqVVNgyrRcYF8/ObNQXaHgX7AOj0aIPY/GXYX873Qjq73Bpc4qGkiYpLfEhslnA2OsemnZjCH83YM88DM83BJLu8KMGklkBS0ndKa2HdKIyTDCwzC6spe5I0mdLRs2MCPPKtUM0vlKajbEAusjjuay0MosN4ZFbf55/H8X83ojyODazturGdGwpiUf4Njt0vWGoX1Y4BDnSv4azkHk7YJSVtraFo/18EbsinOdsh9Z6BS4eyQzRs2p9TXJw9Ta0ECs2GGm4YOQIDAQABoz8wPTAdBgNVHQ4EFgQUCatvZ3g2zyGrvp7m6OFy3B/Z5lcwDwYDVR0TBAgwBgEB/wIBADALBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQEMBQADggGBAJBomC72fdKsOnDRwHXKK2FfbdjknCqy/Sw/TCzvcFmOXS8g4m/D7e3j3z5FKkdDwPKZtZkgWmGn4HU0rJSkxi1D9YAQ3oGsGALOFnhWJxrh9I6bMvOYE4HyaFrQM3XOIGCQ4SEkNqaveGtnZnU7nJCNXmARryIjIvdkicgURxD3XrXswjmkyhjsTItozkK8dVxD73rSYjkX4GFh1ABxL7ABj09w7rcmCzsjqGfVlfHWVXLM3dnQMNgGjFs8GY5TZc85M0FGxX3QQPFP0sxgwZahRhgaio53CfsdIwS5XlIm1T6H0SSkfGy7vzinQLM7oMsHjBQLtl3axe8oA5JznozfGKKOCoNb71lsLCLuzaI1ayLDyekb6L4GqCQNg0QbPx/5NlyzraA1YYgHhiJMXUqcudJHPoVQuKyHeRQ7RcQewZucKuk/IxGBctSF+kUPvlCIyIn9hR4dtQ4XUV9SLczln48fthdwsv3UX0g/i1eyQFwytIcbPFipSxZ09/i2Gg==-----END CERTIFICATE-----');

-- INSERT INTO deployment(id, database_id, service_id, path, created, db_user, db_password, db_name)
-- VALUES ('df237850-2ca3-46d0-a804-2401b7e778d5', 'aa6ceae7-3485-4c54-98dd-2e5ddde4fe61',
--         '2d6aaf50-0799-4bc2-983a-4b1393b1a1c4', '/wp-11430e8b-090b-4562-8354-4522d37fdde2', now(), 'jdgmvuhcai',
--         'fomlmjytdow', 'euoxlarvltgpdrqo');