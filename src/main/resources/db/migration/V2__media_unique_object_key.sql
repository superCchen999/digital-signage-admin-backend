ALTER TABLE media
    ADD CONSTRAINT uk_media_organization_object_key UNIQUE (organization_id, object_key);
