create schema checklists;

GRANT CREATE,USAGE ON SCHEMA checklists TO checklists;

alter user checklists set search_path to checklists;

GRANT USAGE ON SCHEMA checklists TO gift_registry;

create table checklists.CHECKLIST_TEMPLATE (
REGISTRY_TYPE text not null,
CHECKLIST_NAME text not null,
DEFAULT_CHECKLIST boolean,
TEMPLATE_ID int not null,
CHECKLIST_ID int not null,
CATEGORY_ID text not null,
CATEGORY_NAME text not null,
CATEGORY_ORDER int not null,
CATEGORY_IMAGE_UR text not null,
SUBCATEGORY_ID text not null,
SUBCATEGORY_NAME text not null,
SUBCATEGORY_ORDER int not null,
SUBCATEGORY_URL text not null,
PLP_PARAM text not null,
CREATED_TS timestamp not null,
UPDATED_TS timestamp not null,
CONSTRAINT CHECKLIST_TEMPLATE_PK PRIMARY KEY (REGISTRY_TYPE, TEMPLATE_ID, CATEGORY_ORDER));


create table checklists.CHECKED_SUBCATEGORIES (
REGISTRY_ID uuid not null,
TEMPLATE_ID int not null,
CHECKLIST_ID text not null,
CREATED_TS timestamp not null,
CREATED_USER text not null,
UPDATED_TS timestamp not null,
UPDATED_USER text not null,
CONSTRAINT CHECKED_SUBCATEGORIES_PK PRIMARY KEY (REGISTRY_ID,TEMPLATE_ID,CHECKLIST_ID));

create table checklists.REGISTRY_CHECKLIST (
REGISTRY_ID uuid not null,
TEMPLATE_ID int not null,
CREATED_TS timestamp not null,
CREATED_USER text not null,
UPDATED_TS timestamp not null,
UPDATED_USER text not null,
CONSTRAINT REGISTRY_CHECKLIST_PK PRIMARY KEY (REGISTRY_ID));
