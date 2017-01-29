CREATE TABLE logging (
    id            INT(11) NOT NULL AUTO_INCREMENT,
    tid           INT(11),
    year          INT(11),
    month         INT(11),
    day           INT(11),
    hh            INT(11),
    mm            INT(11),
    ss            INT(11),
    date          VARCHAR(255),
    level         VARCHAR(10),
    annotatorType VARCHAR(10),
    processType   VARCHAR(10),
    casIndex      INT(11),
    data          TEXT,
    PRIMARY KEY (id)
);