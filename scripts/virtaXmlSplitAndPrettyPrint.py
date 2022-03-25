import xml.etree.ElementTree as ET
from argparse import ArgumentParser

def sortchildrenby(parent, attr):
    parent[:] = sorted(parent, key=lambda child: child.get(attr))

def sortilmo(parent):
    parent[:] = sorted(parent, key=lambda child: child.find('{*}AlkuPvm').text)

def parsi_molemmat_xmlt(path):
    with open(path, encoding = 'utf-8') as f:
        sisalto = f.read()
    jakaja = '</SOAP-ENV:Envelope>'
    part1, part2 = sisalto.split(jakaja, 1)
    return part1 + jakaja, part2

def splittaa_ja_formatoi(path):
    output1 = path.replace('.xml', '_formatoitu_1.xml')
    output2 = path.replace('.xml', '_formatoitu_2.xml')
    if path == output1:
        print("Osataan käsitellä vain .xml päätteisiä tiedostoja")
    input1, input2 = parsi_molemmat_xmlt(path)
    formatoi(input1, output1)
    formatoi(input2, output2)

def formatoi(input, output):
    root = ET.fromstring(input)

    opiskeluioikeudet = root.findall('{*}Body/{*}OpiskelijanKaikkiTiedotResponse/{*}Virta/{*}Opiskelija/{*}Opiskeluoikeudet')
    for opiskeluioikeus in opiskeluioikeudet:
        sortchildrenby(opiskeluioikeus, 'avain')

    lukukausi_ilmoittautumiset = root.findall('{*}Body/{*}OpiskelijanKaikkiTiedotResponse/{*}Virta/{*}Opiskelija/{*}LukukausiIlmoittautumiset')
    for lukukausi_ilmoittautuminen in lukukausi_ilmoittautumiset:
        sortilmo(lukukausi_ilmoittautuminen)

    opintosuoritukset = root.findall('{*}Body/{*}OpiskelijanKaikkiTiedotResponse/{*}Virta/{*}Opiskelija/{*}Opintosuoritukset')
    for opintosuoritus in opintosuoritukset:
        sortchildrenby(opintosuoritus, 'avain')

    liikkuvuusjaksot = root.findall('{*}Body/{*}OpiskelijanKaikkiTiedotResponse/{*}Virta/{*}Opiskelija/{*}Liikkuvuusjaksot')
    for liikkuvuusjakso in liikkuvuusjaksot:
        sortchildrenby(liikkuvuusjakso, 'avain')

    with open(output, encoding = 'utf-8', mode='w') as f:
        for line in ET.tostring(root, encoding='unicode').splitlines():
            f.write(line.strip()+ '\n')
    print('Tiedosto: ' + output)


if __name__ == '__main__':
    parser = ArgumentParser(description="Jakaa virkailijan käyttöliittymästä saatavan Virta XML kahtia ja formatoi niin, että niitä on helppo verrata")
    parser.add_argument('path')
    args = parser.parse_args()
    splittaa_ja_formatoi(args.path)

