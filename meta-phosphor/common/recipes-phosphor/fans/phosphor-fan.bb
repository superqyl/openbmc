SUMMARY = "Phosphor Fan"
DESCRIPTION = "Phosphor fan provides a set of fan monitoring and \
control applications."
PR = "r1"

require ${PN}.inc

inherit autotools pkgconfig pythonnative
inherit obmc-phosphor-systemd
inherit phosphor-fan

S = "${WORKDIR}/git"

# Common build dependencies
DEPENDS += "autoconf-archive-native"
DEPENDS += "python-pyyaml-native"
DEPENDS += "python-mako-native"
DEPENDS += "sdbusplus"
DEPENDS += "phosphor-logging"

# Package configuration
FAN_PACKAGES = " \
        ${PN}-presence-tach \
        ${PN}-control \
        phosphor-chassis-cooling-type \
"
PACKAGES_remove = "${PN}"
PACKAGES += "${FAN_PACKAGES}"
PACKAGECONFIG ??= "presence control cooling-type"
SYSTEMD_PACKAGES = "${FAN_PACKAGES}"
RDEPENDS_${PN}-dev = "${FAN_PACKAGES}"
RDEPENDS_${PN}-staticdev = "${FAN_PACKAGES}"

# --------------------------------------
# ${PN}-presence-tach specific configuration
PACKAGECONFIG[presence] = " \
        --enable-presence \
	FAN_DETECT_YAML_FILE=${STAGING_DIR_NATIVE}${presence_datadir}/config.yaml, \
        --disable-presence, \
        virtual/phosphor-fan-presence-config \
        , \
"
RDEPENDS_${PN}-presence-tach += "sdbusplus"

# Needed to install into the obmc-chassis-poweron target
TMPL_TACH = "phosphor-fan-presence-tach@.service"
INSTFMT_TACH = "phosphor-fan-presence-tach@{0}.service"
TGTFMT = "obmc-chassis-poweron@{0}.target"
FMT_TACH = "../${TMPL_TACH}:${TGTFMT}.requires/${INSTFMT_TACH}"

FILES_${PN}-presence-tach = "${sbindir}/phosphor-fan-presence-tach"
SYSTEMD_SERVICE_${PN}-presence-tach += "${TMPL_TACH}"
SYSTEMD_LINK_${PN}-presence-tach += "${@compose_list(d, 'FMT_TACH', 'OBMC_CHASSIS_INSTANCES')}"

# --------------------------------------
# ${PN}-control specific configuration
PACKAGECONFIG[control] = "--enable-control \
     FAN_DEF_YAML_FILE=${STAGING_DIR_NATIVE}${control_datadir}/fans.yaml \
     FAN_ZONE_YAML_FILE=${STAGING_DIR_NATIVE}${control_datadir}/zones.yaml \
     FAN_ZONE_OUTPUT_DIR=${S}/control, \
    --disable-control, \
    virtual/phosphor-fan-control-fan-config \
    phosphor-fan-control-zone-config-native \
    , \
"

RDEPENDS_${PN}-control += "sdbusplus"

TMPL_CONTROL = "phosphor-fan-control@.service"
INSTFMT_CONTROL = "phosphor-fan-control@{0}.service"
FMT_CONTROL = "../${TMPL_CONTROL}:${TGTFMT}.requires/${INSTFMT_CONTROL}"

FILES_${PN}-control = "${sbindir}/phosphor-fan-control"
SYSTEMD_SERVICE_${PN}-control += "${TMPL_CONTROL}"
SYSTEMD_LINK_${PN}-control += "${@compose_list(d, 'FMT_CONTROL', 'OBMC_CHASSIS_INSTANCES')}"

# --------------------------------------
# phosphor-chassis-cooling-type specific configuration
PACKAGECONFIG[cooling-type] = "--enable-cooling-type,--disable-cooling-type,libevdev,"
RDEPENDS_phosphor-chassis-cooling-type += "libevdev"
FILES_phosphor-chassis-cooling-type = "${sbindir}/phosphor-cooling-type"