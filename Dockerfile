# Katana's environment (docs/ROADMAP.md 0.3, docs/AGENT.md AGT-07): one image, pinned by digest, in
# which a clean clone builds and tests with the network off. An agent never installs tooling.
#
# The base pins Ubuntu 24.04 (the study's release), JDK 21 (Temurin) and Gradle 8.14.3 in one digest;
# Gradle's own downloads are blocked by the proxy the project works behind, so it comes in the image.
FROM gradle:8.14.3-jdk21-noble@sha256:21bd311ed01360c189b8870c6b6e988199ff10f72d445d02fb39d3cff9da91d7

# GnuCOBOL 3.1.2, the cobc lane (R-003): the Ubuntu package the study's NIST run used.
RUN apt-get update \
 && apt-get install -y --no-install-recommends gnucobol3=3.1.2-5.1ubuntu1 \
 && rm -rf /var/lib/apt/lists/* \
 && cobc --version | grep -q '3.1.2'

# Any user may run the image over a mounted clone; Gradle keeps its state off the clone.
ENV GRADLE_USER_HOME=/tmp/gradle-home
WORKDIR /katana
CMD ["gradle", "build", "--offline"]
