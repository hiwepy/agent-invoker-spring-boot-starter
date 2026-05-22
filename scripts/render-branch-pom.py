#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
按分支名重写 agent-invoker-spring-boot-starter 的 pom.xml。

JDK 基线（与 agent-invoker-java-sdk 一致）:
  2.3.x / 2.7.x -> JDK 8
  3.0.x-4.0.x -> JDK 17

用法: python3 scripts/render-branch-pom.py <branch>
"""
from __future__ import annotations

import os
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
POM = ROOT / "pom.xml"

SNAPSHOT_SUFFIX = f"{os.environ.get('RELEASE_DATE', '20260520')}-SNAPSHOT"

ALIYUN_DM = """
    <distributionManagement>
        <repository>
            <id>2624322-release-6F6h6R</id>
            <url>https://packages.aliyun.com/6927b116e6c3e0425dbdf60d/maven/2624322-release-6f6h6r</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
        <snapshotRepository>
            <id>2624322-snapshot-3EoOv3</id>
            <url>https://packages.aliyun.com/6927b116e6c3e0425dbdf60d/maven/2624322-snapshot-3eoov3</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <uniqueVersion>true</uniqueVersion>
        </snapshotRepository>
    </distributionManagement>
"""

MATRIX = {
    "2.3.x": ("2.3.12.RELEASE", "1.8", "2.3.x", False),
    "2.7.x": ("2.7.18", "11", "2.7.x", True),
    "3.0.x": ("3.0.13", "17", "3.0.x", True),
    "3.1.x": ("3.1.12", "17", "3.1.x", True),
    "3.2.x": ("3.2.12", "17", "3.2.x", True),
    "3.3.x": ("3.3.13", "17", "3.3.x", True),
    "3.4.x": ("3.4.13", "17", "3.4.x", True),
    "3.5.x": ("3.5.9", "17", "3.5.x", True),
    "4.0.x": ("4.0.1", "17", "4.0.x", True),
}


def compiler_config(*, java_version: str, use_release: bool) -> str:
    if use_release:
        return f"""                <configuration>
                    <release>{java_version}</release>
                    <encoding>${{project.build.sourceEncoding}}</encoding>
                </configuration>"""
    return f"""                <configuration>
                    <source>{java_version}</source>
                    <target>{java_version}</target>
                    <encoding>${{project.build.sourceEncoding}}</encoding>
                </configuration>"""


def write_pom(*, boot_parent: str, java_version: str, version_prefix: str, use_release: bool) -> None:
    ver = f"{version_prefix}.{SNAPSHOT_SUFFIX}"
    comp = compiler_config(java_version=java_version, use_release=use_release)
    body = f'''<?xml version='1.0' encoding='UTF-8'?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>{boot_parent}</version>
        <relativePath/>
    </parent>

    <groupId>io.github.hiwepy</groupId>
    <artifactId>agent-invoker-spring-boot-starter</artifactId>
    <version>{ver}</version>
    <packaging>jar</packaging>
    <name>${{project.groupId}}:${{project.artifactId}}</name>
    <description>Spring Boot starter for AI Agent Invoker — multi-provider AI agent invocation (line {version_prefix}; JDK {java_version})</description>
    <url>https://github.com/hiwepy/${{project.artifactId}}</url>

    <properties>
        <java.version>{java_version}</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <agent-invoker-java-sdk.version>{ver}</agent-invoker-java-sdk.version>
        <openclaw-spring-boot-starter.version>{ver}</openclaw-spring-boot-starter.version>
        <maven-source-plugin.version>3.3.1</maven-source-plugin.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>io.github.hiwepy</groupId>
            <artifactId>agent-invoker-java-sdk</artifactId>
            <version>${{agent-invoker-java-sdk.version}}</version>
        </dependency>
        <dependency>
            <groupId>io.github.hiwepy</groupId>
            <artifactId>openclaw-spring-boot-starter</artifactId>
            <version>${{openclaw-spring-boot-starter.version}}</version>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
{ALIYUN_DM}

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
{comp}
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <skipTests>false</skipTests>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>${{maven-source-plugin.version}}</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <goals>
                            <goal>jar-no-fork</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-deploy-plugin</artifactId>
                <version>3.1.2</version>
            </plugin>
        </plugins>
    </build>
</project>
'''
    POM.write_text(body, encoding="utf-8")


def render(branch: str) -> None:
    if branch == "main":
        branch = "3.3.x"
    if branch not in MATRIX:
        keys = ", ".join(sorted(MATRIX))
        raise SystemExit(f"unsupported branch: {branch}. Choose one of: {keys}")
    boot, jdk, prefix, use_release = MATRIX[branch]
    write_pom(boot_parent=boot, java_version=jdk, version_prefix=prefix, use_release=use_release)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__.strip(), file=sys.stderr)
        sys.exit(2)
    render(sys.argv[1])
