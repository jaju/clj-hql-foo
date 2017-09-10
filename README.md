# msync.hql-foo

A small utility to analyze HQL statements for table dependencies.

## Usage

TBD. But a sample file /sample.hql/ is provided, and the script /hql-scanner.sh/ can be invoked with the file as its argument to see the output.

```clojure
(process-file "sample.hql")
=>
({:create #{"some_external_table"}, :insert #{}, :referred #{}}
 {:create #{"some_generated_data_table"}, :insert #{}, :referred #{"some_external_table" "some_other_external_table"}}
 {:create #{}, :insert #{"some_generated_data_table"}, :referred #{"some_external_table" "some_other_external_table"}})
```

## License

Copyright © 2017 -- Ravindra R. Jaju

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
