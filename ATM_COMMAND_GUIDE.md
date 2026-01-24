# ATM Command Guide

## Overview
The `/atm` command opens a beautiful ATM terminal GUI for players to manage their finances.

## Features
- **Superb GUI Design**: Professional-looking ATM interface with colored buttons and decorative elements
- **Multiple Options**: Deposit, Withdraw, Check Balance, Transfer, and Transaction History buttons
- **Visual Feedback**: Sound effects and welcome messages
- **Ready for Expansion**: Easy to add functionality to each button

## How to Use

### For Players
1. Type `/atm` in chat
2. The ATM terminal GUI will open
3. Click on any button to use that feature (functionality coming soon!)

### For Server Admins
The command is available to all players by default. No special permissions are required.

## GUI Layout

The ATM terminal features:
- **Center Display**: Shows ATM status and connection info
- **Menu Buttons** (top row):
  - Green: Deposit
  - Orange: Withdraw
  - Yellow: Check Balance
  - Cyan: Transfer
  - Purple: Transaction History
- **Decorative Elements**:
  - Gold corners for premium look
  - LED lights for modern feel
  - Screen border for visual separation
  - Currency icons showing accepted payment methods

## Technical Details

### Command Registration
- Command: `/atm`
- Class: `AtmCommand.java`
- Location: `src/main/java/com/extraction/commands/AtmCommand.java`

### GUI Specifications
- Inventory Size: 54 slots (5 rows × 9 columns)
- Background: Black stained glass for dark, professional look
- Button Materials: Colored terracotta for easy identification

## Future Enhancements

The ATM command is ready to be expanded with:
1. **Deposit Functionality**: Convert items to currency
2. **Withdraw Functionality**: Exchange currency for items
3. **Balance Check**: Display current account balance
4. **Transfer System**: Send money to other players
5. **Transaction History**: View past transactions

## Building the Plugin

To rebuild the plugin after modifications:
```bash
cd Documents\Builds\Extraction\Extraction_plugin
mvn clean package
```

The compiled JAR will be in: `target/extraction-1.0-SNAPSHOT.jar`

## Notes

- The ATM GUI is purely visual at this stage
- All buttons are clickable but don't perform actions yet
- The design is optimized for Minecraft 1.21.11
- Sound effects use `BLOCK_NOTE_BLOCK_BELL` for a pleasant chime
