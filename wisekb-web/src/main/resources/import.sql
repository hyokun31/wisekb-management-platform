-- Table: flamingo.fl_workflow

DROP TABLE flamingo.fl_workflow;

CREATE TABLE flamingo.fl_workflow
(
  id bigint NOT NULL DEFAULT nextval('flamingo."FL_WORKFLOW_ID_seq"'::regclass),
  workflow_id character varying NOT NULL,
  workflow_name character varying NOT NULL, -- Workflow Name
  description character varying DEFAULT ''::character varying, -- Description
  variable text,
  workflow_xml text NOT NULL,
  designer_xml text NOT NULL, -- Designer XML
  create_dt timestamp with time zone DEFAULT now(), -- Workflow Variable
  status character varying NOT NULL,
  tree_id bigint NOT NULL,
  username character varying NOT NULL,
  CONSTRAINT "FL_WORKFLOW_pkey" PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE flamingo.fl_workflow
  OWNER TO flamingo;
COMMENT ON TABLE flamingo.fl_workflow
  IS 'flamingo workflow table';
COMMENT ON COLUMN flamingo.fl_workflow.workflow_name IS 'Workflow Name';
COMMENT ON COLUMN flamingo.fl_workflow.description IS 'Description';
COMMENT ON COLUMN flamingo.fl_workflow.designer_xml IS 'Designer XML';
COMMENT ON COLUMN flamingo.fl_workflow.create_dt IS 'Workflow Variable';

-- Table: flamingo.uima_log

CREATE TABLE public.uima_log
(
  log_process_id integer,
  log_process_type character(20),
  log_level character(20),
  log_collection_reader character(20),
  log_ip character(20),
  log_annotator_type character(20),
  log_data character(100),
  log_date timestamp without time zone,
  log_index integer NOT NULL,
  CONSTRAINT uima_log_pkey PRIMARY KEY (log_index)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.uima_log
  OWNER TO flamingo;

INSERT INTO public.uima_log(
            log_process_id, log_process_type, log_level, log_collection_reader,
            log_ip, log_annotator_type, log_data, log_date, log_index)
    VALUES (1, 'LOAD', 'INFO', 'collectionreader',
            'xxx.xxx.xxx.xxx', 'TEST1', '그건 그렇고 너 이번에 그 WIS (War in Space) 아마추어 세계 대회 출전할 거지?', '2016-12-26 10:00:00', 1);


INSERT INTO public.uima_log(
            log_process_id, log_process_type, log_level, log_collection_reader,
            log_ip, log_annotator_type, log_data, log_date, log_index)
    VALUES (1, 'PROCESSING', 'INFO', 'collectionreader',
            'xxx.xxx.xxx.xxx', 'TEST1', '그건 그렇고 너 이번에 그 WIS (War in Space) 아마추어 세계 대회 출전할 거지?', '2016-12-26 11:00:00', 2);


INSERT INTO public.uima_log(
            log_process_id, log_process_type, log_level, log_collection_reader,
            log_ip, log_annotator_type, log_data, log_date, log_index)
    VALUES (1, 'DONE', 'INFO', 'collectionreader',
            'xxx.xxx.xxx.xxx', 'TEST1', '그건 그렇고 너 이번에 그 WIS (War in Space) 아마추어 세계 대회 출전할 거지?', '2016-12-26 12:00:00', 3);


INSERT INTO public.uima_log(
            log_process_id, log_process_type, log_level, log_collection_reader,
            log_ip, log_annotator_type, log_data, log_date, log_index)
    VALUES (2, 'LOAD', 'INFO', 'collectionreader',
            'xxx.xxx.xxx.xxx', 'TEST1', '그건 그렇고 너 이번에 그 WIS (War in Space) 아마추어 세계 대회 출전할 거지?', '2016-12-27 10:00:00', 4);


INSERT INTO public.uima_log(
            log_process_id, log_process_type, log_level, log_collection_reader,
            log_ip, log_annotator_type, log_data, log_date, log_index)
    VALUES (2, 'PROCESSING', 'INFO', 'collectionreader',
            'xxx.xxx.xxx.xxx', 'TEST1', '그건 그렇고 너 이번에 그 WIS (War in Space) 아마추어 세계 대회 출전할 거지?', '2016-12-27 11:00:00', 5);


INSERT INTO public.uima_log(
            log_process_id, log_process_type, log_level, log_collection_reader,
            log_ip, log_annotator_type, log_data, log_date, log_index)
    VALUES (2, 'DONE', 'INFO', 'collectionreader',
            'xxx.xxx.xxx.xxx', 'TEST1', '그건 그렇고 너 이번에 그 WIS (War in Space) 아마추어 세계 대회 출전할 거지?', '2016-12-27 12:00:00', 6);


INSERT INTO public.uima_log(
            log_process_id, log_process_type, log_level, log_collection_reader,
            log_ip, log_annotator_type, log_data, log_date, log_index)
    VALUES (3, 'LOAD', 'INFO', 'collectionreader',
            'xxx.xxx.xxx.xxx', 'TEST1', '그건 그렇고 너 이번에 그 WIS (War in Space) 아마추어 세계 대회 출전할 거지?', '2016-12-26 11:00:00', 7);


INSERT INTO public.uima_log(
            log_process_id, log_process_type, log_level, log_collection_reader,
            log_ip, log_annotator_type, log_data, log_date, log_index)
    VALUES (3, 'PROCESSING', 'INFO', 'collectionreader',
            'xxx.xxx.xxx.xxx', 'TEST1', '그건 그렇고 너 이번에 그 WIS (War in Space) 아마추어 세계 대회 출전할 거지?', '2016-12-26 16:00:00', 8);