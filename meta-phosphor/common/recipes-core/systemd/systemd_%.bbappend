FILES_${PN}-catalog-extralocales = \
            "${exec_prefix}/lib/systemd/catalog/*.*.catalog"
PACKAGES =+ "${PN}-catalog-extralocales"
PACKAGECONFIG_append = " networkd coredump"
PACKAGECONFIG_remove = "machined hibernate ldconfig binfmt backlight localed \
                        quotacheck kdbus ima smack polkit logind bootchart utmp"
FILESEXTRAPATHS_append := "${THISDIR}/${PN}:"
SRC_URI += "file://default.network"
SRC_URI += "file://service-restart-policy.conf"
SRC_URI += "file://0001-Export-message_append_cmdline.patch"
SRC_URI += "file://0002-systemd-Make-pam-compile-shared-library.patch"
SRC_URI += "file://0003-basic-Factor-out-string-checking-from-name_to_prefix.patch"
SRC_URI += "file://0004-basic-Use-path-escaping-when-mangling-path-instances.patch"
#TODO upstream the below patch via below issue
#https://github.com/openbmc/openbmc/issues/2016
SRC_URI += "file://0005-dont-return-error-if-unable-to-create-network-namespace.patch"
SRC_URI += "file://0006-journal-Create-journald-dbus-object.patch"
SRC_URI += "file://0007-journal-Add-Synchronize-dbus-method.patch"
SRC_URI += "${@mf_enabled(d, 'obmc-ubi-fs', 'file://software.conf')}"
SRC_URI += "file://0008-man-update-machine-id-5-with-a-note-about-privacy-46.patch"
SRC_URI += "file://0009-sd-id128-add-new-sd_id128_get_machine_app_specific-A.patch"
SRC_URI += "file://0010-core-add-khash-API-to-src-basic-as-wrapper-around-ke.patch"

RRECOMMENDS_${PN} += "obmc-targets"
FILES_${PN} += "${libdir}/systemd/network/default.network"
FILES_${PN} += "${libdir}/systemd/system.conf.d/service-restart-policy.conf"

EXTRA_OECONF += " --disable-hwdb"

do_install_append() {
        install -m 644 ${WORKDIR}/default.network ${D}${libdir}/systemd/network/
        install -m 644 -D ${WORKDIR}/service-restart-policy.conf ${D}${libdir}/systemd/system.conf.d/service-restart-policy.conf

        #TODO Remove after this issue is resolved
        #https://github.com/openbmc/openbmc/issues/152
        ln -s /dev/null ${D}/etc/systemd/system/systemd-hwdb-update.service

        # /tmp/images is the software image upload directory.
        # It should not be deleted since it is watched by the Image Manager
        # for new images.
        if ${@bb.utils.contains('MACHINE_FEATURES', 'obmc-ubi-fs', 'true', 'false', d)}; then
                install -m 0644 ${WORKDIR}/software.conf ${D}${exec_prefix}/lib/tmpfiles.d/
        fi
}
