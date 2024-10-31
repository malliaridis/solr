# Changes

## Changes between Legacy and New CLI

- `bin/solr snapshot-create` is now `bin/solr snapshot create`
- `bin/solr snapshot-delete` is now `bin/solr snapshot delete`
- `bin/solr snapshot-describe` is now `bin/solr snapshot describe`
- `bin/solr snapshot-list` is now `bin/solr snapshot list`
- `bin/solr snapshot-export` is now `bin/solr snapshot export`
- `bin/solr cluster` is now `bin/solr zk cluster`
- `bin/solr start -e *` is now `bin/solr start example *`
- `--jettyconfig` is now `--jetty-config`
- `solr.ulimit.checks` is now `solr.ulimit.checks.enabled`
- `SOLR_TIP` is now `SOLR_INSTALL_DIR`
- `SOLR_TIP` is now `SOLR_INSTALL_DIR`
- `-DdisableAdminUI` is now `solr.admin.enabled`
- `SOLR_HEAP` is now `solr.memory.allocation.max` and `solr.memory.allocation.initial`
