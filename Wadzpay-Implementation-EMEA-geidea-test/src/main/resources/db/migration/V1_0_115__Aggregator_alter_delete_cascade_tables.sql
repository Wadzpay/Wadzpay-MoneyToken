ALTER TABLE institution
DROP CONSTRAINT fk_instition_aggregator_id,
ADD CONSTRAINT fk_instition_aggregator_id
  FOREIGN KEY (aggregator_preference_id)
  REFERENCES aggregator(aggregator_preference_id)
  ON DELETE CASCADE;


ALTER TABLE merchant_group
DROP CONSTRAINT fk_merchant_group_instition_pref_id,
ADD CONSTRAINT fk_merchant_group_instition_pref_id
  FOREIGN KEY (aggregator_preference_id, insitution_preference_id)
  REFERENCES institution (aggregator_preference_id, institution_id)
  ON DELETE CASCADE;


ALTER TABLE merchant_acquirer
DROP CONSTRAINT fk_merchant_aquirer_mrchnt_grp_pref_id,
ADD CONSTRAINT fk_merchant_aquirer_mrchnt_grp_pref_id
  FOREIGN KEY (aggregator_preference_id, insitution_preference_id,merchant_group_preference_id)
  REFERENCES merchant_group (aggregator_preference_id,insitution_preference_id,merchant_group_preference_id)
  ON DELETE CASCADE;
