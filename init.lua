box.cfg{
    listen = 3301,
    memtx_memory = 1024 * 1024 * 1024  -- 1 ГБ
}

box.once("schema_init", function()
    box.schema.user.grant('guest', 'read,write,execute', 'universe')

    local kv = box.schema.space.create('KV', {
        format = {
            {name = 'key',    type = 'string'},
            {name = 'value',  type = 'varbinary', is_nullable = true}
        }
    })

    kv:create_index('primary', {
        type = 'TREE',
        parts = {'key'}
    })

    print('=== Tarantool KV space initialized ===')
end)

print('=== Tarantool is ready to accept requests ===')

require('fiber').sleep(math.huge)