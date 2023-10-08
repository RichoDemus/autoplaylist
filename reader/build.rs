use static_files::resource_dir;

fn main() {
    resource_dir("./server/core/spring/src/main/resources/static")
        .build()
        .expect("loading resource failed");
}
