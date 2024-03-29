# Snowflake input plugin for Embulk
[![Java CI with Gradle](https://github.com/estie-inc/embulk-input-snowflakedb/actions/workflows/test.yml/badge.svg)](https://github.com/estie-inc/embulk-input-snowflakedb/actions/workflows/test.yml)
[![Gem Version](https://img.shields.io/gem/v/embulk-input-snowflakedb)](https://rubygems.org/gems/embulk-input-snowflakedb)

Snowflake input plugin for Embulk loads records from Snowflake.

## Overview

* **Plugin type**: input

## Configuration

- **host**: database host name (string, required)
- **user**: database login user name (string, required)
- **password**: database login password (string, default: "")
- **database**: destination database name (string, required)
- **schema**: destination schema name (string, required)
- **role**: role to execute queries (string, optional)
- If **query** is not set,
    - **table**: destination table name (string, required)
    - **select**: expression of select (e.g. `id, created_at`) (string, default: "*")
    - **where**: WHERE condition to filter the rows (string, default: no-condition)
    - **order_by**: expression of ORDER BY to sort rows (e.g. `created_at DESC, id ASC`) (string, default: not sorted)
      `timestamp` are supported.
- **privateKeyPath**: path to private key file (string, optional)
- **privateKeyPassphrase**: passphrase of private key file (string, optional). If the private key file is not encrypted,
  this option can be omitted.
- **privateKey**: private key string (string, optional). You can use this option instead of `privateKeyPath`. If you want to
  put the private key from environment variable, you can use `"{{ env.PRIVATE_KEY | newline_to_br | replace: '<br />', '\n' }}"` syntax.

## Build

```shell
$ ./gradlew gem
```

## Test

```shell
$ ./gradlew test
```

## Publish gem to RubyGems.org

```shell
$ ./gradlew gemPush
```
