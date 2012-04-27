

drop table if exists data_source_to_unique_field;
create table data_source_to_unique_field (
  data_source_to_unique_field_id bigint(20) auto_increment not null,
  data_source_id bigint(20) not null,
  child_source_id bigint(20) not null,
  analysis_item_id bigint(20) not null,
  primary key (data_source_to_unique_field_id),
  constraint data_source_to_unique_field_ibfk1 foreign key (data_source_id) references data_feed (data_feed_id) on delete cascade,
  constraint data_source_to_unique_field_ibfk2 foreign key (analysis_item_id) references analysis_item (analysis_item_id) on delete cascade,
  constraint data_source_to_unique_field_ibfk3 foreign key (child_source_id) references data_feed (data_feed_id) on delete cascade
) type=InnoDB;