CREATE TABLE IF NOT EXISTS `mp_groups` (
        group_id VARCHAR(128) NOT NULL,
        prefix TEXT,
        suffix TEXT,
        --Indexes
        INDEX (group_id),
        PRIMARY KEY(group_id)) ENGINE = INNODB;