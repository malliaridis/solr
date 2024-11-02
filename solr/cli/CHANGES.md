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
- Environment variable `STOP_KEY` is now `SOLR_STOP_KEY`
- System property `STOP.KEY` is now `solr.stop.key`
- Default stop key is now `SolrRocks` (previously `solrrocks`)
- Environment variable `STOP_PORT` is now `SOLR_STOP_PORT`
- System property `STOP.PORT` is now `solr.stop.port`
- Environment variable `ZK_CREATE_CHROOT` is now `SOLR_ZK_CREATE_CHROOT`
- Environment variable `ENABLE_REMOTE_JMX_OPTS` is now `SOLR_JMX_ENABLED`
- System property `solr.jetty.inetaccess.includes` is now `solr.security.acl.allow`
- System property `solr.jetty.inetaccess.excludes` is now `solr.security.acl.deny`
- Environemnt variable `GC_LOG_OPTS` is now `SOLR_GC_LOG_OPTS`
- System property `solr.placementplugin.default` is now `solr.plugins.placement`
  - value is still mapped on start via CLI to old property for compatibility
- System property `solr.enableRemoteStreaming` is now `solr.features.remoteStreaming.enabled`
    - value is still mapped on start via CLI to old property for compatibility
- System property `solr.enableRemoteStreaming` is now `solr.features.remoteStreaming.enabled` and
  `solr.features.remoteStreaming.mode` 
  - value is still mapped on start via CLI to old property for compatibility
- System property `solr.enableStreamBody` is now `solr.features.streamBody.enabled`
  - value is still mapped on start via CLI to old property for compatibility
- System property `solr.port` is now `solr.port.bind` in favor to `solr.port.advertise`
- Value of environment variable `SOLR_JAVA_STACK_SIZE` is now value of `-Xss`, like `256k`
- Setting `SOLR_SSL_KEY_STORE` is not automatically enabling SSL
- Environment variable `SOLR_SSL_NEED_CLIENT_AUTH` is now `SOLR_SSL_CLIENT_NEED_AUTH`
- System property `solr.jetty.ssl.needClientAuth` is now `solr.ssl.client.needAuth`
  - value is still mapped on start via CLI to old property for compatibility
- Environment variable `SOLR_HEAP_DUMP` is now `SOLR_HEAP_DUMP_ENABLED` in favor to `SOLR_HEAP_DUMP_DIR`
- Heap dump must now be explicitly enabled via `SOLR_HEAP_DUMP_ENABLED`
  - Setting `SOLR_HEAP_DUMP_DIR` will not add additional properties if heap dump not enabled
- System property `solr.httpclient.builder.factory` is now `solr.auth.client.builder`
  - value is still mapped on start via CLI to old property for compatibility
