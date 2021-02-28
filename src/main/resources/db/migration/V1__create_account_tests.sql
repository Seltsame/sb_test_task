CREATE TABLE IF NOT EXISTS account
(

    id             BIGSERIAL PRIMARY KEY,
    account_number varchar(25) NOT NULL,
    open_date      date        NOT NULL,
    close_date     date
)