ALTER TABLE institution
    ADD CONSTRAINT unique_columns_constraint UNIQUE (aggregator_preference_id, insitution_preference_id);
