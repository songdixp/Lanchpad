farm:
	@npx hardhat compile
	@npx hardhat run --network local scripts/deployment/deploy_c2n_token.js
	@npx hardhat run --network local scripts/deployment/deploy_airdrop_c2n.js
	@npx hardhat run --network local scripts/deployment/deploy_farm.js
ido:
	@npx hardhat compile
	@npx hardhat run --network local scripts/deployment/deploy_c2n_token.js
	@npx hardhat run --network local scripts/deployment/deploy_airdrop_c2n.js
	@npx hardhat run --network local scripts/deployment/deploy_farm.js
	@npx hardhat run --network local scripts/deployment/deploy_ido.js
	@npx hardhat run --network local scripts/deployment/deploy_sales_token.js
	@npx hardhat run --network local scripts/deployment/deploy_sales.js
	@npx hardhat run --network local scripts/deployment/deploy_tge.js
upgrades:
	@npx hardhat compile
	@npx hardhat run --network local scripts/management/upgradeAllocationStaking.js