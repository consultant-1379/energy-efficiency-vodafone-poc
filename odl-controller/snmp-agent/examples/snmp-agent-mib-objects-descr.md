MIB: ifMIB
Table: IfTable
Object-Type: ifOutOctets
OID: 1.3.6.1.2.1.2.2.1.16.<ifIndex>
Syntax: Counter64
Description: "The total number of octets transmitted out of the
              interface, including framing characters." 


MIB: ifMIB
Table: IfTable
Object-Type: ifInOctets
OID: 1.3.6.1.2.1.2.2.1.10.<ifIndex>
Syntax: Counter64
Description:  "The total number of octets received on the interface,
               including framing characters." 


MIB: ifMIB
Table: IfTable
Object-type: ifSpeed
OID: 1.3.6.1.2.1.31.1.1.1.5.<IfIndex>
Syntax: Gauge32
Description: "An estimate of the interface's current bandwidth in
              bits per second. For interfaces which do not vary in
              bandwidth or for those where no accurate estimation
              can be made, this object should contain the nominal
              bandwidth. If the bandwidth of the interface is
              greater than the maximum value reportable by this
              object then this object should report its maximum
              value (4,294,967,295) and ifHighSpeed must be used to
              report the interace's speed. For a sub-layer which
              has no concept of bandwidth, this object should be
              zero." 


MIB: ifMIB
Table: IfXTable
Object-Type: ifHCOutOctets
OID: 1.3.6.1.2.1.31.1.1.1.10.<ifIndex>
Syntax: Counter64
Description:   "The total number of octets transmitted out of the
                interface, including framing characters. This object
                is a 64-bit version of ifOutOctets." 


MIB: ifMIB
Table: IfXTable
Object-Type: ifHCInOctets
OID: 1.3.6.1.2.1.31.1.1.1.6.<ifIndex>
Syntax: Counter64
Description:  "The total number of octets received on the interface,
               including framing characters. This object is a 64-bit
               version of ifInOctets."


MIB: ifMIB
Table: IfXTable
Object-type: ifHighSpeed
OID: 1.3.6.1.2.1.31.1.1.1.15.<IfIndex>
Syntax: Gauge32
Description: "An estimate of the interface's current bandwidth in
              units of 1,000,000 bits per second. If this object
              reports a value of `n' then the speed of the interface
              is somewhere in the range of `n-500,000' to
              `n+499,999'. For interfaces which do not vary in
              bandwidth or for those where no accurate estimation
              can be made, this object should contain the nominal
              bandwidth. For a sub-layer which has no concept of
              bandwidth, this object should be zero."


MIB: xfRadioLinkPtpRadioMIB
Table: xfRfIfPowerTable
Object-Type: xfRfSelectedMinOutputPower
OID: 1.3.6.1.4.1.193.81.3.4.3.1.8.1.1.<IfIndex>
Syntax: RfOutputPower
Description:  "Minimum output power in dBm (selected by operator).
               Default value: 10 dBm."


MIB: xfRadioLinkPtpRadioMIB
Table: xfRfIfPowerTable
Object-Type: xfRfSelectedMaxOutputPower
OID: 1.3.6.1.4.1.193.81.3.4.3.1.8.1.2.<IfIndex>
Syntax: RfOutputPower
Description:  "Maximum output power in dBm (selected by operator).
               Default value: 10 dBm."


MIB: xfRadioLinkPtpRadioMIB
Table: xfRfIfPowerTable
Object-Type: xfRfCurrentOutputPower
OID: 1.3.6.1.4.1.193.81.3.4.3.1.8.1.3.<IfIndex>
Syntax: RfOutputPower (Integer32 (-100..35|255))
Description: "Current output power in dBm. 255 indicates invalid value."


MIB: xfRadioLinkPtpRadioMIB
Table: xfRfIfPowerTable
Object-Type: xfRfAtpcTargetInputPowerFE
OID: 1.3.6.1.4.1.193.81.3.4.3.1.8.1.6.<IfIndex>
Syntax: Integer32 (-99 .. -30)
Description:  "ATPC target input power on Far End side. Default value: -30." 


MIB: xfRadioLinkPtpRadioMIB
Table: xfRFIFTable
Object-Type: xfRFTxAdminStatus
OID: 1.3.6.1.4.1.193.81.3.4.3.1.2.1.8.<IfIndex>
Syntax: INTEGER  { other ( 1 ) , txOff ( 2 ) , txOn ( 3 ) } 
Description:  "This object sets the desired status of the transmitter.
               Value other(1) not settable.
               Especially for far end:
                 txOff(2), requests far end transmitter to be turned off.
                 txOn(3), requests far end transmitter to be turned on.
                 Default value: txOff(2) - near end.
                 other(1) - far end."


MIB: xfRadioLinkPtpRadioMIB
Table: xfRFIFTable
Object-Type: xfRFTxOperStatus
OID: 1.3.6.1.4.1.193.81.3.4.3.1.2.1.7.<IfIndex>
Syntax: RFTxOperStatus {other(1), txOff(2), txOn(3), txStandby(4)}
Description: "This object shows the operational status of the transmitter."


MIB: xfRadioLinkRltMIB
Table: xfCarrierTerminationTable 
Object-Type: xfCarrierTermSelectedMinACM
OID: 1.3.6.1.4.1.193.81.3.4.5.1.3.1.6.<entLogicalIndex>
Syntax: CarrierTermACMIndex
        SYNTAX INTEGER
        {
            acm4QAMStrong(1),
            acm4QAMStd(2),
            acm4QAMLight(3),
            acm16QAMStrong(4),
            acm16QAMStd(5),
            acm16QAMLight(6),
            acm32QAMStrong(7),
            acm32QAMStd(8),
            acm32QAMLight(9),
            acm64QAMStrong(10),
            acm64QAMStd(11),
            acm64QAMLight(12),
            acm128QAMStrong(13),
            acm128QAMStd(14),
            acm128QAMLight(15),
            acm256QAMStrong(16),
            acm256QAMStd(17),
            acm256QAMLight(18),
            acm512QAMStrong(19),
            acm512QAMStd(20),
            acm512QAMLight(21),
            acm1024QAMStrong(22),
            acm1024QAMStd(23),
            acm1024QAMLight(24),
            acm2048QAMStrong(25),
            acm2048QAMStd(26),
            acm2048QAMLight(27),
            acm4096QAMStrong(28),
            acm4096QAMStd(29),
            acm4096QAMLight(30)
        }
Description: "It represents the selected minimum Tx ACM (Adaptive Code Modulation)."


MIB: xfRadioLinkRltMIB
Table: xfCarrierTerminationTable 
Object-Type: xfCarrierTermActualACM
OID: 1.3.6.1.4.1.193.81.3.4.5.1.3.1.7.<entLogicalIndex>
Syntax: CarrierTermACMIndex (see above details)
Description: ""It represents the actual (current) Tx ACM (Adaptive Code Modulation)."


MIB: xfRadioLinkRltMIB
Table: xfCarrierTerminationTable 
Object-Type: xfCarrierTermSelectedMinACM
OID: 1.3.6.1.4.1.193.81.3.4.5.1.3.1.8.<entLogicalIndex>
Syntax: CarrierTermACMIndex
Description: "It represents the selected maximum Tx ACM (Adaptive Code Modulation)."


MIB: xfRadioLinkRltMIB
Table: xfCarrierTerminationTable 
Object-Type: xfCarrierTermRadioFrameId
OID: 1.3.6.1.4.1.193.81.3.4.5.1.3.1.4.<entLogicalIndex>
Syntax: Integer32
Description: "It represents the selected Radio Frame ID."


MIB: xfRadioLinkRltMIB
Table: xfCarrierTerminationCapabilityTable 
Object-Type: xfChannelSpacing
OID: 1.3.6.1.4.1.193.81.3.4.5.1.4.1.2.<entLogicalIndex>
Syntax: INTEGER
        {
            chspOther(1),
            chsp3500kHz(2),
            chsp7MHz(3),
            chsp10MHz(4),
            chsp14MHz(5),
            chsp20MHz(6),
            chsp28MHz(7),
            chsp30MHz(8),
            chsp40MHz(9),
            chsp50MHz(10),
            chsp56MHz(11),
            chsp60MHz(12),
            chsp80MHz(13),
            chsp112MHz(14)
        }
Description:  "Channel spacing:
               - ETSI: 3.5, 7, 14, 28, 40, 56, 112 MHz
               - ANSI: 10, 20, 30, 40, 50, 60, 80 MHz"
               

MIB: xfRadioLinkRltMIB
Table: xfRLWANIfTable
Object-type: xfRLWANActualCapacity
OID: 1.3.6.1.4.1.193.81.3.4.5.1.12.1.8.<IfIndex> (Note: WLAN i/f IfIndex)
Syntax: Integer32
Description: "Actual Capacity (speed) of RL WAN interface, in kbps."


MIB: xfRadioLinkRltMIB
Table: xrRLTTable
Object-type: xfRLTActualTXTotalCapacity 
OID: 1.3.6.1.4.1.193.81.3.4.5.1.6.1.12.<rltEntLogicalIndex>
Syntax: Integer32
Description: "It represents the total transmitted capacity throughput (considering both Packet and TDM traffic),
              expressed in Kbps, of the Radio Link Terminal (RLT)." 

Note: 
  rltEntLogicalIndex (Integer32)
  "It represents the entLogicalIndex of the Radio Link Terminal (RLT).
  The logical entity identified by rltEntLogicalIndex is the same
  entity as identified by the same value of entLogicalIndex." 


MIB: xfRadioLinkRltMIB
Table: xrRLTTable
Object-type: xfRLTActualTXPacketCapacity 
OID: 1.3.6.1.4.1.193.81.3.4.5.1.6.1.13.<rltEntLogicalIndex>
Syntax: Integer32
Description:  "It represents the transmitted packet capacity throughput (only packet traffic),
               expressed in Kbps, of the Radio Link Terminal (RLT)." 


MIB: xfEthernetInterfacesMIB
Table: xfEthernetIfXTable
Object-type: xfEthernetIfMinSpeed
OID: 1.3.6.1.4.1.193.81.4.1.3.1.1.1.1.5.<IfIndex>
Syntax: Integer32
Description: "The minimum speed of this interface. Same units as ifSpeed."


MIB: xfEthernetInterfacesMIB
Table: xfEthernetIfXTable
Object-type: xfEthernetIfMaxSpeed
OID: 1.3.6.1.4.1.193.81.4.1.3.1.1.1.1.6.<IfIndex>
Syntax: Integer32
Description: "The maximum speed of this interface. Same units as ifSpeed." 



