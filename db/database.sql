-- account (user) table
create table accounts
(
    uid        text                                      not null
        constraint accounts_pk
            primary key,
    email      text                                      not null
        constraint accounts_pk_2_email
            unique,
    password   text,
    name       text                                      not null,
    avatar     text,
    verified   boolean      default false                not null,
    role       account_role default 'USER'::account_role not null,
    created_at timestamp    default CURRENT_TIMESTAMP    not null,
    updated_at timestamp    default CURRENT_TIMESTAMP    not null,
    archived   boolean      default false                not null
);

comment on table accounts is 'the user of the application';

comment on column accounts.password is 'the password of this account, can be null if user login using OAuth2';

comment on column accounts.avatar is 'avatar url of the user';

comment on column accounts.verified is 'true if the email has been verified';

comment on column accounts.role is 'the role of the user in the application';

comment on column accounts.archived is 'true if this account has been archived';

alter table accounts
    owner to root;

-- automatically update the updated_at column on update
create or replace function update_updated_at_column()
    returns trigger as $$
begin
    new.updated_at = current_timestamp;
    return new;
end;
$$ language plpgsql;

create trigger set_updated_at
    before update on accounts
    for each row
execute function update_updated_at_column();
