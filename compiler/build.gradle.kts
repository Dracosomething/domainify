tasks.register<JavaExec>("execute") {
    mainModule = "compiler"
    mainClass = "io.github.dracosomething.Build"
}

tasks.register("compile") {
    dependsOn("jar")
    dependsOn("execute")
}
