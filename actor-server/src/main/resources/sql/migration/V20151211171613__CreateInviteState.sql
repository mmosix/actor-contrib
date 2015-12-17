create table invite_states(
    id bigint not null,
    inviter_user_id int not null,
    invitee_email varchar(255) not null,
    invitee_name varchar(255),
    team_id int,
    created_at timestamp not null,
    is_accepted boolean not null,
    primary key(id)
);