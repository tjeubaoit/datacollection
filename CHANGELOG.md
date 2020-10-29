# Data Collection

## Version 0.8.2 - 19/10/2017

### Added

- New job: extract history in Cassandra to Kafka to re-process

# Updated

- Update job: sync profiles, fetch facebook avatar
- Clean code, fix some minor bugs

## Version 0.8.1 - 11/10/2017

### Added

- New repository: HBase
- Add job: fetch facebook avatar url
- Add authentication for mongodb

### Updated

- Update jobs: extract_address, identify_fbid, sync_profile use repository pattern
- Update model: use 64bit id instead of UUID
- Update datacollection-common to version 1.1.4

## Version 0.7.2 - 01/10/2017

### Added

- Add new forum extractors
- Add excel normalizer

### Updated

- Update forum collectors (compatible with new model structure)
- Update tools: extract address, merge per-app facebook id use Kafka

## Version 0.7.1 - 25/9/2017

### Added

- Tool convert Cassandra update_time to update_time_string (used for secondary index)
- Tool sync profile from Cassandra to ElasticSearch

### Updated

- Fix bugs
- Update datacollection-common to version 1.1.2

## Version 0.7.0 - 6/9/2017

### Added

- Address extract tool
- Merge per-app facebook id tool
- Support hystrix monitoring for collectors