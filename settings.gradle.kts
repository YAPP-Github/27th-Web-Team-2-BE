rootProject.name = "Yapp"

include(
    "adapter:oauth",
    "adapter:rdb",
    "app:api",
    "core",
    "domain",
    "port",
    "support:logging",
    "support:yaml",
)
