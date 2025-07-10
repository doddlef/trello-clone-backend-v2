-- Database schema for the application
create type account_role as enum ('ADMIN', 'USER');

CREATE TYPE account_status AS ENUM (
    'ACTIVE',
    'LOCKED',
    'ARCHIVED'
);

-- account (user) table
create table accounts
(
    uid        text                                            not null
        constraint accounts_pk
            primary key,
    email      text                                            not null
        constraint accounts_pk_2_email
            unique,
    password   text,
    nickname   text                                            not null,
    avatar     text,
    verified   boolean        default false                    not null,
    role       account_role   default 'USER'::account_role     not null,
    created_at timestamp      default CURRENT_TIMESTAMP        not null,
    updated_at timestamp      default CURRENT_TIMESTAMP        not null,
    status     account_status default 'ACTIVE'::account_status not null
);

comment on table accounts is 'the user of the application';

comment on column accounts.password is 'the password of this account, can be null if user login using OAuth2';

comment on column accounts.avatar is 'avatar url of the user';

comment on column accounts.verified is 'true if the email has been verified';

comment on column accounts.role is 'the role of the user in the application';

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

-- Email active token table
create table email_active_tokens
(
    id         serial
        constraint email_active_tokens_pk
            primary key,
    content    text                                not null
        constraint email_active_tokens_pk_content
            unique,
    uid        text                                not null
        constraint email_active_tokens_pk_account
            unique
        constraint email_active_tokens___fk_account
            references accounts,
    expired_at timestamp                           not null,
    created_at timestamp default CURRENT_TIMESTAMP not null
);

comment on table email_active_tokens is 'store tokens used verification and active emails';

comment on column email_active_tokens.uid is 'the uid of account that related to this token';

alter table email_active_tokens
    owner to root;

create index email_active_tokens_content_index
    on email_active_tokens (content);

-- Refresh token table
create table refresh_tokens
(
    id         serial
        constraint refresh_tokens_pk
            primary key,
    content    text                                not null
        constraint refresh_tokens_pk_content
            unique,
    uid        text                                not null
        constraint refresh_tokens___fk_account
            references accounts,
    expired_at timestamp                           not null,
    created_at timestamp default CURRENT_TIMESTAMP not null
);

comment on table refresh_tokens is 'stores token used to refresh access token';

alter table refresh_tokens
    owner to root;

-- membership role types
CREATE TYPE membership_role AS ENUM (
    'ADMIN',
    'MEMBER'
        'VIEWER'
    );

-- board table
create table boards
(
    id          text                                not null
        constraint boards_pk
            primary key,
    title       text                                not null,
    description text,
    closed      boolean   default false             not null,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    updated_at  timestamp default CURRENT_TIMESTAMP not null,
    created_by  text                                not null
        constraint boards___fk_creator
            references accounts,
    archived    boolean   default false             not null
);

alter table boards
    owner to root;

create trigger set_updated_at
    before update on boards
    for each row
execute function update_updated_at_column();

-- board members table
create table board_members
(
    board_id   text                                not null
        constraint board_members___fk_board
            references boards,
    user_uid   text                                not null
        constraint board_members___fk_user
            references accounts,
    role       membership_role                     not null,
    starred    boolean   default false             not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null,
    active     boolean   default true              not null,
    constraint board_members_pk
        primary key (board_id, user_uid)
);

alter table board_members
    owner to root;

create trigger set_updated_at
    before update on board_members
    for each row
execute function update_updated_at_column();

-- board views table
create view board_views(board_id, title, description, closed, user_uid, role, starred) as
SELECT b.id AS board_id,
       b.title,
       b.description,
       b.closed,
       m.user_uid,
       m.role,
       m.starred
FROM boards b
         JOIN board_members m ON b.id = m.board_id
WHERE m.active = true;

alter table board_views
    owner to root;